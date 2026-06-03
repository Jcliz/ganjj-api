jest.mock('../../shared/db');
jest.mock('../http/productClient');

const db = require('../../shared/db');
const productClient = require('../http/productClient');
const { createPedido, getPedido } = require('../controllers/pedidoController');

function mockRes() {
  const res = {};
  res.status = jest.fn().mockReturnValue(res);
  res.json = jest.fn().mockReturnValue(res);
  return res;
}

let mockConn;

beforeEach(() => {
  mockConn = { query: jest.fn(), release: jest.fn() };
  db.connect.mockResolvedValue(mockConn);
});

// ─── createPedido ─────────────────────────────────────────────────────────────

describe('createPedido', () => {
  test('retorna 400 se itens não fornecidos', async () => {
    const req = { usuario: { id: 1 }, body: {} };
    const res = mockRes();

    await createPedido(req, res);

    expect(res.status).toHaveBeenCalledWith(400);
    expect(res.json).toHaveBeenCalledWith({ error: 'Itens do pedido são obrigatórios' });
  });

  test('retorna 400 se itens é array vazio', async () => {
    const req = { usuario: { id: 1 }, body: { itens: [] } };
    const res = mockRes();

    await createPedido(req, res);

    expect(res.status).toHaveBeenCalledWith(400);
    expect(res.json).toHaveBeenCalledWith({ error: 'Itens do pedido são obrigatórios' });
  });

  test('retorna 400 se item está incompleto (sem preco)', async () => {
    const req = { usuario: { id: 1 }, body: { itens: [{ produto_id: 1, quantidade: 2 }] } };
    const res = mockRes();

    await createPedido(req, res);

    expect(res.status).toHaveBeenCalledWith(400);
    expect(res.json).toHaveBeenCalledWith({
      error: 'Cada item deve ter produto_id, quantidade e preco',
    });
  });

  test('retorna 404 se produto não encontrado no product-service', async () => {
    const req = { usuario: { id: 1 }, body: { itens: [{ produto_id: 99, quantidade: 1, preco: 50 }] } };
    const res = mockRes();
    const err = new Error('Produto não encontrado');
    err.status = 404;
    productClient.decrementStock.mockRejectedValueOnce(err);

    await createPedido(req, res);

    expect(res.status).toHaveBeenCalledWith(404);
    expect(res.json).toHaveBeenCalledWith(
      expect.objectContaining({ error: expect.stringContaining('99') })
    );
  });

  test('retorna 400 se estoque insuficiente', async () => {
    const req = { usuario: { id: 1 }, body: { itens: [{ produto_id: 1, quantidade: 20, preco: 50 }] } };
    const res = mockRes();
    const err = new Error('Estoque insuficiente');
    err.estoque_disponivel = 5;
    productClient.decrementStock.mockRejectedValueOnce(err);

    await createPedido(req, res);

    expect(res.status).toHaveBeenCalledWith(400);
    expect(res.json).toHaveBeenCalledWith(
      expect.objectContaining({
        error: expect.stringContaining('Estoque insuficiente'),
        estoque_disponivel: 5,
      })
    );
  });

  test('compensa estoque dos itens já decrementados quando um item falha', async () => {
    const req = {
      usuario: { id: 1 },
      body: {
        itens: [
          { produto_id: 1, quantidade: 1, preco: 50 },
          { produto_id: 2, quantidade: 1, preco: 50 },
        ],
      },
    };
    const res = mockRes();
    const err = new Error('Estoque insuficiente');
    productClient.decrementStock
      .mockResolvedValueOnce({})
      .mockRejectedValueOnce(err);
    productClient.restoreStock.mockResolvedValue({});

    await createPedido(req, res);

    expect(productClient.restoreStock).toHaveBeenCalledWith(1, 1);
    expect(res.status).toHaveBeenCalledWith(400);
  });

  test('cria pedido com sucesso e retorna 201', async () => {
    const criado_em = new Date();
    const req = {
      usuario: { id: 1 },
      body: { itens: [{ produto_id: 1, quantidade: 2, preco: 50 }] },
    };
    const res = mockRes();
    productClient.decrementStock.mockResolvedValueOnce({});
    mockConn.query
      .mockResolvedValueOnce({ rows: [{ id: 1, status: 'pending', total: '100.00', criado_em }] })
      .mockResolvedValueOnce({});

    await createPedido(req, res);

    expect(res.status).toHaveBeenCalledWith(201);
    expect(res.json).toHaveBeenCalledWith(
      expect.objectContaining({
        id: 1,
        codigo: '#GNJ-00001',
        status: 'pending',
        total: 100,
      })
    );
    expect(mockConn.release).toHaveBeenCalled();
  });

  test('restaura estoque e retorna 500 em caso de erro no banco', async () => {
    const req = {
      usuario: { id: 1 },
      body: { itens: [{ produto_id: 1, quantidade: 1, preco: 50 }] },
    };
    const res = mockRes();
    productClient.decrementStock.mockResolvedValueOnce({});
    productClient.restoreStock.mockResolvedValue({});
    mockConn.query.mockRejectedValueOnce(new Error('DB error'));

    await createPedido(req, res);

    expect(productClient.restoreStock).toHaveBeenCalledWith(1, 1);
    expect(res.status).toHaveBeenCalledWith(500);
  });
});

// ─── getPedido ────────────────────────────────────────────────────────────────

describe('getPedido', () => {
  test('retorna 404 se pedido não encontrado', async () => {
    const req = { params: { id: '99' } };
    const res = mockRes();
    mockConn.query.mockResolvedValueOnce({ rows: [] });

    await getPedido(req, res);

    expect(res.status).toHaveBeenCalledWith(404);
    expect(res.json).toHaveBeenCalledWith({ error: 'Pedido não encontrado' });
  });

  test('retorna pedido com itens', async () => {
    const req = { params: { id: '1' } };
    const res = mockRes();
    const criado_em = new Date();
    mockConn.query
      .mockResolvedValueOnce({
        rows: [{ id: 1, total: '100.00', status: 'pending', criado_em, cliente: 'João', email: 'a@b.com' }],
      })
      .mockResolvedValueOnce({
        rows: [{ quantidade: 2, preco: '50.00', nome: 'Camiseta', imagem_url: null }],
      });

    await getPedido(req, res);

    expect(res.json).toHaveBeenCalledWith(
      expect.objectContaining({
        id: 1,
        codigo: '#GNJ-00001',
        total: 100,
        itens: expect.arrayContaining([
          expect.objectContaining({ nome: 'Camiseta', quantidade: 2, preco: 50 }),
        ]),
      })
    );
    expect(mockConn.release).toHaveBeenCalled();
  });
});
