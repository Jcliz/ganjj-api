jest.mock('../../shared/db');

const db = require('../../shared/db');
const { listarSale } = require('../controllers/saleController');

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

const ROW = {
  id: 1, nome: 'Camiseta', descricao: null, preco: '100.00', estoque: 5,
  cor: 'preto', status: true, imagem_url: null, popular: false, feminino: false,
  criado_em: new Date(), sale_id: 9, desconto_pct: 30, categoria: 'Superiores',
  preco_sale: '70.00',
};

describe('listarSale', () => {
  test('lista produtos em sale sem filtro de categoria', async () => {
    const req = { query: {} };
    const res = mockRes();
    mockConn.query.mockResolvedValueOnce({ rows: [ROW] });

    await listarSale(req, res);

    const [sql, params] = mockConn.query.mock.calls[0];
    expect(sql).not.toContain('s.categoria = $');
    expect(params).toEqual([]);
    expect(res.json).toHaveBeenCalledWith([
      expect.objectContaining({ id: 1, preco: 100, preco_sale: 70, desconto_pct: 30 }),
    ]);
    expect(mockConn.release).toHaveBeenCalled();
  });

  test('filtra por categoria quando informada', async () => {
    const req = { query: { categoria: 'Inverno' } };
    const res = mockRes();
    mockConn.query.mockResolvedValueOnce({ rows: [] });

    await listarSale(req, res);

    const [sql, params] = mockConn.query.mock.calls[0];
    expect(sql).toContain('s.categoria = $1');
    expect(params).toEqual(['Inverno']);
    expect(res.json).toHaveBeenCalledWith([]);
  });

  test('ignora o filtro quando categoria é "Todos"', async () => {
    const req = { query: { categoria: 'Todos' } };
    const res = mockRes();
    mockConn.query.mockResolvedValueOnce({ rows: [] });

    await listarSale(req, res);

    const [, params] = mockConn.query.mock.calls[0];
    expect(params).toEqual([]);
  });

  test('retorna 500 em caso de erro no banco', async () => {
    const req = { query: {} };
    const res = mockRes();
    mockConn.query.mockRejectedValueOnce(new Error('DB error'));

    await listarSale(req, res);

    expect(res.status).toHaveBeenCalledWith(500);
    expect(res.json).toHaveBeenCalledWith({ error: 'Erro ao buscar produtos em sale' });
  });
});
