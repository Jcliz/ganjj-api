jest.mock('../../shared/db');
jest.mock('../http/productClient');

const db = require('../../shared/db');
const productClient = require('../http/productClient');
const {
  getCesta,
  adicionarItem,
  atualizarItem,
  removerItem,
  limparCesta,
} = require('../controllers/cestaController');

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

// ─── getCesta ─────────────────────────────────────────────────────────────────

describe('getCesta', () => {
  test('retorna itens vazios se usuário não tem cesta', async () => {
    const req = { usuario: { id: 1 } };
    const res = mockRes();
    mockConn.query.mockResolvedValueOnce({ rows: [] });

    await getCesta(req, res);

    expect(res.json).toHaveBeenCalledWith({ itens: [] });
    expect(mockConn.release).toHaveBeenCalled();
  });

  test('retorna itens da cesta do usuário', async () => {
    const req = { usuario: { id: 1 } };
    const res = mockRes();
    mockConn.query
      .mockResolvedValueOnce({ rows: [{ id: 5 }] })
      .mockResolvedValueOnce({
        rows: [{ id: 10, produto_id: 2, quantidade: 1, nome: 'Camiseta', preco: '50.00', cor: 'azul', imagem_url: null }],
      });

    await getCesta(req, res);

    expect(res.json).toHaveBeenCalledWith({
      itens: [
        expect.objectContaining({ produto_id: 2, nome: 'Camiseta', preco: 50 }),
      ],
    });
  });
});

// ─── adicionarItem ────────────────────────────────────────────────────────────

describe('adicionarItem', () => {
  test('retorna 400 se produto_id não fornecido', async () => {
    const req = { usuario: { id: 1 }, body: { quantidade: 2 } };
    const res = mockRes();

    await adicionarItem(req, res);

    expect(res.status).toHaveBeenCalledWith(400);
    expect(res.json).toHaveBeenCalledWith(
      expect.objectContaining({ error: expect.stringContaining('obrigatórios') })
    );
  });

  test('retorna 400 se quantidade menor que 1', async () => {
    const req = { usuario: { id: 1 }, body: { produto_id: 1, quantidade: 0 } };
    const res = mockRes();

    await adicionarItem(req, res);

    expect(res.status).toHaveBeenCalledWith(400);
  });

  test('retorna 404 se produto não encontrado no product-service', async () => {
    const req = { usuario: { id: 1 }, body: { produto_id: 99, quantidade: 1 } };
    const res = mockRes();
    const err = new Error('Produto não encontrado');
    err.status = 404;
    productClient.checkStock.mockRejectedValueOnce(err);

    await adicionarItem(req, res);

    expect(res.status).toHaveBeenCalledWith(404);
    expect(res.json).toHaveBeenCalledWith({ error: 'Produto não encontrado' });
  });

  test('retorna 400 se estoque insuficiente', async () => {
    const req = { usuario: { id: 1 }, body: { produto_id: 1, quantidade: 10 } };
    const res = mockRes();
    productClient.checkStock.mockResolvedValueOnce({ estoque: 3 });

    await adicionarItem(req, res);

    expect(res.status).toHaveBeenCalledWith(400);
    expect(res.json).toHaveBeenCalledWith({ error: 'Estoque insuficiente' });
  });

  test('adiciona item com sucesso e retorna 201', async () => {
    const req = { usuario: { id: 1 }, body: { produto_id: 1, quantidade: 2 } };
    const res = mockRes();
    productClient.checkStock.mockResolvedValueOnce({ estoque: 10 });
    mockConn.query
      .mockResolvedValueOnce({ rows: [{ id: 5 }] })  // carrinho existente
      .mockResolvedValueOnce({ rows: [] })           // item ainda não está na cesta
      .mockResolvedValueOnce({});                    // INSERT

    await adicionarItem(req, res);

    expect(res.status).toHaveBeenCalledWith(201);
    expect(res.json).toHaveBeenCalledWith({ message: 'Item adicionado à cesta' });
    expect(mockConn.release).toHaveBeenCalled();
  });
});

// ─── atualizarItem ────────────────────────────────────────────────────────────

describe('atualizarItem', () => {
  test('retorna 400 se quantidade inválida', async () => {
    const req = { usuario: { id: 1 }, params: { produto_id: '1' }, body: { quantidade: 0 } };
    const res = mockRes();

    await atualizarItem(req, res);

    expect(res.status).toHaveBeenCalledWith(400);
    expect(res.json).toHaveBeenCalledWith({ error: 'Quantidade deve ser maior que zero' });
  });

  test('retorna 404 se cesta não encontrada', async () => {
    const req = { usuario: { id: 1 }, params: { produto_id: '1' }, body: { quantidade: 2 } };
    const res = mockRes();
    mockConn.query.mockResolvedValueOnce({ rows: [] });

    await atualizarItem(req, res);

    expect(res.status).toHaveBeenCalledWith(404);
    expect(res.json).toHaveBeenCalledWith({ error: 'Cesta não encontrada' });
  });

  test('retorna 404 se item não está na cesta', async () => {
    const req = { usuario: { id: 1 }, params: { produto_id: '99' }, body: { quantidade: 2 } };
    const res = mockRes();
    mockConn.query
      .mockResolvedValueOnce({ rows: [{ id: 5 }] })
      .mockResolvedValueOnce({ rows: [] });

    await atualizarItem(req, res);

    expect(res.status).toHaveBeenCalledWith(404);
    expect(res.json).toHaveBeenCalledWith({ error: 'Item não encontrado na cesta' });
  });

  test('atualiza quantidade do item com sucesso', async () => {
    const req = { usuario: { id: 1 }, params: { produto_id: '1' }, body: { quantidade: 3 } };
    const res = mockRes();
    mockConn.query
      .mockResolvedValueOnce({ rows: [{ id: 5 }] })
      .mockResolvedValueOnce({ rows: [{ id: 10 }] });

    await atualizarItem(req, res);

    expect(res.json).toHaveBeenCalledWith({ message: 'Quantidade atualizada' });
    expect(mockConn.release).toHaveBeenCalled();
  });
});

// ─── removerItem ──────────────────────────────────────────────────────────────

describe('removerItem', () => {
  test('retorna 404 se cesta não encontrada', async () => {
    const req = { usuario: { id: 1 }, params: { produto_id: '1' } };
    const res = mockRes();
    mockConn.query.mockResolvedValueOnce({ rows: [] });

    await removerItem(req, res);

    expect(res.status).toHaveBeenCalledWith(404);
    expect(res.json).toHaveBeenCalledWith({ error: 'Cesta não encontrada' });
  });

  test('remove item com sucesso', async () => {
    const req = { usuario: { id: 1 }, params: { produto_id: '1' } };
    const res = mockRes();
    mockConn.query
      .mockResolvedValueOnce({ rows: [{ id: 5 }] })
      .mockResolvedValueOnce({});

    await removerItem(req, res);

    expect(res.json).toHaveBeenCalledWith({ message: 'Item removido da cesta' });
    expect(mockConn.release).toHaveBeenCalled();
  });
});

// ─── limparCesta ──────────────────────────────────────────────────────────────

describe('limparCesta', () => {
  test('limpa a cesta do usuário', async () => {
    const req = { usuario: { id: 1 } };
    const res = mockRes();
    mockConn.query
      .mockResolvedValueOnce({ rows: [{ id: 5 }] })  // carrinho existente
      .mockResolvedValueOnce({});                    // DELETE

    await limparCesta(req, res);

    expect(res.json).toHaveBeenCalledWith({ message: 'Cesta limpa' });
    expect(mockConn.release).toHaveBeenCalled();
  });
});
