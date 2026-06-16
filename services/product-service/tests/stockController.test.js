jest.mock('../../shared/db');

const db = require('../../shared/db');
const { verificarInterno, getStock, decrementStock, restoreStock } = require('../controllers/stockController');

function mockRes() {
  const res = {};
  res.status = jest.fn().mockReturnValue(res);
  res.json = jest.fn().mockReturnValue(res);
  return res;
}

let mockConn;

beforeEach(() => {
  process.env.INTERNAL_SECRET = 'segredo-interno';
  mockConn = { query: jest.fn(), release: jest.fn() };
  db.connect.mockResolvedValue(mockConn);
});

// ─── verificarInterno ─────────────────────────────────────────────────────────

describe('verificarInterno', () => {
  test('retorna 403 se o secret interno não confere', () => {
    const req = { headers: { 'x-internal-secret': 'errado' } };
    const res = mockRes();
    const next = jest.fn();

    verificarInterno(req, res, next);

    expect(res.status).toHaveBeenCalledWith(403);
    expect(next).not.toHaveBeenCalled();
  });

  test('chama next se o secret interno confere', () => {
    const req = { headers: { 'x-internal-secret': 'segredo-interno' } };
    const res = mockRes();
    const next = jest.fn();

    verificarInterno(req, res, next);

    expect(next).toHaveBeenCalled();
  });
});

// ─── getStock ─────────────────────────────────────────────────────────────────

describe('getStock', () => {
  test('retorna 404 se produto não encontrado', async () => {
    const req = { params: { id: '99' } };
    const res = mockRes();
    mockConn.query.mockResolvedValueOnce({ rows: [] });

    await getStock(req, res);

    expect(res.status).toHaveBeenCalledWith(404);
  });

  test('retorna o estoque do produto', async () => {
    const req = { params: { id: '1' } };
    const res = mockRes();
    mockConn.query.mockResolvedValueOnce({ rows: [{ id: 1, nome: 'Camiseta', estoque: 8 }] });

    await getStock(req, res);

    expect(res.json).toHaveBeenCalledWith({ id: 1, nome: 'Camiseta', estoque: 8 });
    expect(mockConn.release).toHaveBeenCalled();
  });

  test('retorna 500 em caso de erro no banco', async () => {
    const req = { params: { id: '1' } };
    const res = mockRes();
    mockConn.query.mockRejectedValueOnce(new Error('DB error'));

    await getStock(req, res);

    expect(res.status).toHaveBeenCalledWith(500);
    expect(res.json).toHaveBeenCalledWith({ error: 'Erro ao buscar estoque' });
  });
});

// ─── decrementStock ───────────────────────────────────────────────────────────

describe('decrementStock', () => {
  test('retorna 400 se quantidade inválida', async () => {
    const req = { params: { id: '1' }, body: { quantidade: 0 } };
    const res = mockRes();

    await decrementStock(req, res);

    expect(res.status).toHaveBeenCalledWith(400);
    expect(res.json).toHaveBeenCalledWith({ error: 'quantidade deve ser maior que zero' });
  });

  test('decrementa o estoque com sucesso', async () => {
    const req = { params: { id: '1' }, body: { quantidade: 2 } };
    const res = mockRes();
    mockConn.query.mockResolvedValueOnce({ rows: [{ id: 1, nome: 'Camiseta', estoque: 6 }] });

    await decrementStock(req, res);

    expect(res.json).toHaveBeenCalledWith({ ok: true, id: 1, estoque: 6 });
  });

  test('retorna 404 se produto não existe', async () => {
    const req = { params: { id: '99' }, body: { quantidade: 1 } };
    const res = mockRes();
    mockConn.query
      .mockResolvedValueOnce({ rows: [] })   // UPDATE não afetou linhas
      .mockResolvedValueOnce({ rows: [] });  // produto não existe

    await decrementStock(req, res);

    expect(res.status).toHaveBeenCalledWith(404);
  });

  test('retorna 400 com estoque disponível se insuficiente', async () => {
    const req = { params: { id: '1' }, body: { quantidade: 10 } };
    const res = mockRes();
    mockConn.query
      .mockResolvedValueOnce({ rows: [] })                          // UPDATE não afetou linhas
      .mockResolvedValueOnce({ rows: [{ id: 1, estoque: 3 }] });    // produto existe com estoque 3

    await decrementStock(req, res);

    expect(res.status).toHaveBeenCalledWith(400);
    expect(res.json).toHaveBeenCalledWith({ error: 'Estoque insuficiente', estoque_disponivel: 3 });
  });

  test('retorna 500 em caso de erro no banco', async () => {
    const req = { params: { id: '1' }, body: { quantidade: 1 } };
    const res = mockRes();
    mockConn.query.mockRejectedValueOnce(new Error('DB error'));

    await decrementStock(req, res);

    expect(res.status).toHaveBeenCalledWith(500);
  });
});

// ─── restoreStock ─────────────────────────────────────────────────────────────

describe('restoreStock', () => {
  test('retorna 400 se quantidade inválida', async () => {
    const req = { params: { id: '1' }, body: {} };
    const res = mockRes();

    await restoreStock(req, res);

    expect(res.status).toHaveBeenCalledWith(400);
  });

  test('restaura o estoque com sucesso', async () => {
    const req = { params: { id: '1' }, body: { quantidade: 2 } };
    const res = mockRes();
    mockConn.query.mockResolvedValueOnce({});

    await restoreStock(req, res);

    expect(res.json).toHaveBeenCalledWith({ ok: true });
    expect(mockConn.release).toHaveBeenCalled();
  });

  test('retorna 500 em caso de erro no banco', async () => {
    const req = { params: { id: '1' }, body: { quantidade: 2 } };
    const res = mockRes();
    mockConn.query.mockRejectedValueOnce(new Error('DB error'));

    await restoreStock(req, res);

    expect(res.status).toHaveBeenCalledWith(500);
  });
});
