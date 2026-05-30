jest.mock('../../db/db');

const db = require('../../db/db');
const { createPedido, getPedido } = require('../../src/controllers/pedidoController');

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
    const req = { body: { usuario_id: 1 } };
    const res = mockRes();

    await createPedido(req, res);

    expect(res.status).toHaveBeenCalledWith(400);
    expect(res.json).toHaveBeenCalledWith({ error: 'Itens do pedido são obrigatórios' });
  });

  test('retorna 400 se itens é array vazio', async () => {
    const req = { body: { usuario_id: 1, itens: [] } };
    const res = mockRes();

    await createPedido(req, res);

    expect(res.status).toHaveBeenCalledWith(400);
    expect(res.json).toHaveBeenCalledWith({ error: 'Itens do pedido são obrigatórios' });
  });

  test('retorna 400 se item está incompleto (sem preco)', async () => {
    const req = { body: { itens: [{ produto_id: 1, quantidade: 2 }] } };
    const res = mockRes();

    await createPedido(req, res);

    expect(res.status).toHaveBeenCalledWith(400);
    expect(res.json).toHaveBeenCalledWith({
      error: 'Cada item deve ter produto_id, quantidade e preco',
    });
  });

  test('retorna 404 se produto não encontrado no estoque', async () => {
    const req = {
      body: { itens: [{ produto_id: 99, quantidade: 1, preco: 50 }] },
    };
    const res = mockRes();
    mockConn.query
      .mockResolvedValueOnce({}) // BEGIN
      .mockResolvedValueOnce({ rows: [] }); // SELECT produto

    await createPedido(req, res);

    expect(res.status).toHaveBeenCalledWith(404);
    expect(res.json).toHaveBeenCalledWith(
      expect.objectContaining({ error: expect.stringContaining('99') })
    );
  });

  test('retorna 400 se estoque insuficiente', async () => {
    const req = {
      body: { itens: [{ produto_id: 1, quantidade: 20, preco: 50 }] },
    };
    const res = mockRes();
    mockConn.query
      .mockResolvedValueOnce({}) // BEGIN
      .mockResolvedValueOnce({ rows: [{ estoque: 5, nome: 'Camiseta' }] }); // SELECT produto

    await createPedido(req, res);

    expect(res.status).toHaveBeenCalledWith(400);
    expect(res.json).toHaveBeenCalledWith(
      expect.objectContaining({
        error: expect.stringContaining('Estoque insuficiente'),
        estoque_disponivel: 5,
      })
    );
  });

  test('cria pedido com sucesso e retorna 201', async () => {
    const criado_em = new Date();
    const req = {
      body: {
        usuario_id: 1,
        itens: [{ produto_id: 1, quantidade: 2, preco: 50 }],
      },
    };
    const res = mockRes();
    mockConn.query
      .mockResolvedValueOnce({}) // BEGIN
      .mockResolvedValueOnce({ rows: [{ estoque: 10, nome: 'Camiseta' }] }) // SELECT produto
      .mockResolvedValueOnce({ rows: [{ id: 1, status: 'pending', total: '100.00', criado_em }] }) // INSERT compra
      .mockResolvedValueOnce({}) // INSERT compra_itens
      .mockResolvedValueOnce({}) // UPDATE estoque
      .mockResolvedValueOnce({}); // COMMIT

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

  test('faz rollback em caso de erro inesperado', async () => {
    const req = {
      body: { itens: [{ produto_id: 1, quantidade: 1, preco: 50 }] },
    };
    const res = mockRes();
    mockConn.query
      .mockResolvedValueOnce({}) // BEGIN
      .mockRejectedValueOnce(new Error('DB error')); // SELECT produto lança erro

    await createPedido(req, res);

    const rollbackCall = mockConn.query.mock.calls.find(
      ([sql]) => typeof sql === 'string' && sql.includes('ROLLBACK')
    );
    expect(rollbackCall).toBeDefined();
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
