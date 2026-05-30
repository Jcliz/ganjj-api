jest.mock('../../db/db');

const db = require('../../db/db');
const {
  createProduto,
  getProdutos,
  getProdutoById,
  updateProduto,
  deleteProduto,
} = require('../../src/controllers/produtoController');

function mockRes() {
  const res = {};
  res.status = jest.fn().mockReturnValue(res);
  res.json = jest.fn().mockReturnValue(res);
  return res;
}

const produtoRow = {
  id: 1,
  nome: 'Camiseta',
  descricao: 'Ótima qualidade',
  preco: '99.90',
  estoque: 10,
  cor: 'azul',
  status: true,
  imagem_url: null,
  popular: false,
  feminino: false,
  criado_em: new Date(),
};

let mockConn;

beforeEach(() => {
  mockConn = { query: jest.fn(), release: jest.fn() };
  db.connect.mockResolvedValue(mockConn);
});

// ─── createProduto ────────────────────────────────────────────────────────────

describe('createProduto', () => {
  test('retorna 400 se campos obrigatórios faltam', async () => {
    const req = { body: { nome: 'Camiseta' } };
    const res = mockRes();

    await createProduto(req, res);

    expect(res.status).toHaveBeenCalledWith(400);
    expect(res.json).toHaveBeenCalledWith(
      expect.objectContaining({ error: expect.stringContaining('obrigatórios') })
    );
  });

  test('retorna 400 para preço negativo', async () => {
    const req = { body: { nome: 'Camiseta', preco: -10, estoque: 5 } };
    const res = mockRes();

    await createProduto(req, res);

    expect(res.status).toHaveBeenCalledWith(400);
    expect(res.json).toHaveBeenCalledWith({ error: 'Preço inválido' });
  });

  test('retorna 400 para preço não numérico', async () => {
    const req = { body: { nome: 'Camiseta', preco: 'abc', estoque: 5 } };
    const res = mockRes();

    await createProduto(req, res);

    expect(res.status).toHaveBeenCalledWith(400);
    expect(res.json).toHaveBeenCalledWith({ error: 'Preço inválido' });
  });

  test('retorna 400 para estoque decimal', async () => {
    const req = { body: { nome: 'Camiseta', preco: 10, estoque: 1.5 } };
    const res = mockRes();

    await createProduto(req, res);

    expect(res.status).toHaveBeenCalledWith(400);
    expect(res.json).toHaveBeenCalledWith({ error: 'Estoque inválido' });
  });

  test('retorna 400 para estoque negativo', async () => {
    const req = { body: { nome: 'Camiseta', preco: 10, estoque: -1 } };
    const res = mockRes();

    await createProduto(req, res);

    expect(res.status).toHaveBeenCalledWith(400);
    expect(res.json).toHaveBeenCalledWith({ error: 'Estoque inválido' });
  });

  test('retorna 400 para status inválido', async () => {
    const req = { body: { nome: 'Camiseta', preco: 10, estoque: 5, status: 'invalido' } };
    const res = mockRes();

    await createProduto(req, res);

    expect(res.status).toHaveBeenCalledWith(400);
    expect(res.json).toHaveBeenCalledWith({ error: 'Status inválido' });
  });

  test('cria produto com sucesso e retorna 201', async () => {
    const req = { body: { nome: 'Camiseta', preco: 99.90, estoque: 10, cor: 'azul' } };
    const res = mockRes();
    mockConn.query.mockResolvedValueOnce({ rows: [produtoRow] });

    await createProduto(req, res);

    expect(res.status).toHaveBeenCalledWith(201);
    expect(res.json).toHaveBeenCalledWith(expect.objectContaining({ nome: 'Camiseta', preco: 99.90 }));
    expect(mockConn.release).toHaveBeenCalled();
  });

  test('aceita status como boolean true', async () => {
    const req = { body: { nome: 'Camiseta', preco: 10, estoque: 5, status: true } };
    const res = mockRes();
    mockConn.query.mockResolvedValueOnce({ rows: [{ ...produtoRow, status: true }] });

    await createProduto(req, res);

    expect(res.status).toHaveBeenCalledWith(201);
  });
});

// ─── getProdutos ──────────────────────────────────────────────────────────────

describe('getProdutos', () => {
  test('retorna lista de produtos', async () => {
    const req = {};
    const res = mockRes();
    mockConn.query.mockResolvedValueOnce({ rows: [produtoRow] });

    await getProdutos(req, res);

    expect(res.json).toHaveBeenCalledWith(
      expect.arrayContaining([expect.objectContaining({ nome: 'Camiseta' })])
    );
    expect(mockConn.release).toHaveBeenCalled();
  });

  test('retorna lista vazia se não há produtos', async () => {
    const req = {};
    const res = mockRes();
    mockConn.query.mockResolvedValueOnce({ rows: [] });

    await getProdutos(req, res);

    expect(res.json).toHaveBeenCalledWith([]);
  });
});

// ─── getProdutoById ───────────────────────────────────────────────────────────

describe('getProdutoById', () => {
  test('retorna 404 se produto não encontrado', async () => {
    const req = { params: { id: '99' } };
    const res = mockRes();
    mockConn.query.mockResolvedValueOnce({ rows: [] });

    await getProdutoById(req, res);

    expect(res.status).toHaveBeenCalledWith(404);
    expect(res.json).toHaveBeenCalledWith({ error: 'Produto não encontrado' });
  });

  test('retorna produto pelo id', async () => {
    const req = { params: { id: '1' } };
    const res = mockRes();
    mockConn.query.mockResolvedValueOnce({ rows: [produtoRow] });

    await getProdutoById(req, res);

    expect(res.json).toHaveBeenCalledWith(expect.objectContaining({ id: 1, nome: 'Camiseta' }));
  });
});

// ─── updateProduto ────────────────────────────────────────────────────────────

describe('updateProduto', () => {
  test('retorna 400 se nenhum campo enviado', async () => {
    const req = { params: { id: '1' }, body: {} };
    const res = mockRes();

    await updateProduto(req, res);

    expect(res.status).toHaveBeenCalledWith(400);
    expect(res.json).toHaveBeenCalledWith({ error: 'Nenhum campo para atualizar' });
  });

  test('retorna 400 para preço inválido', async () => {
    const req = { params: { id: '1' }, body: { preco: 'abc' } };
    const res = mockRes();

    await updateProduto(req, res);

    expect(res.status).toHaveBeenCalledWith(400);
    expect(res.json).toHaveBeenCalledWith({ error: 'Preço inválido' });
  });

  test('retorna 400 para estoque inválido', async () => {
    const req = { params: { id: '1' }, body: { estoque: -5 } };
    const res = mockRes();

    await updateProduto(req, res);

    expect(res.status).toHaveBeenCalledWith(400);
    expect(res.json).toHaveBeenCalledWith({ error: 'Estoque inválido' });
  });

  test('retorna 404 se produto não encontrado', async () => {
    const req = { params: { id: '99' }, body: { nome: 'Novo Nome' } };
    const res = mockRes();
    mockConn.query.mockResolvedValueOnce({ rows: [] });

    await updateProduto(req, res);

    expect(res.status).toHaveBeenCalledWith(404);
    expect(res.json).toHaveBeenCalledWith({ error: 'Produto não encontrado' });
  });

  test('atualiza produto com sucesso', async () => {
    const req = { params: { id: '1' }, body: { nome: 'Novo Nome', preco: 120 } };
    const res = mockRes();
    mockConn.query.mockResolvedValueOnce({
      rows: [{ ...produtoRow, nome: 'Novo Nome', preco: '120.00' }],
    });

    await updateProduto(req, res);

    expect(res.json).toHaveBeenCalledWith(expect.objectContaining({ nome: 'Novo Nome' }));
    expect(mockConn.release).toHaveBeenCalled();
  });
});

// ─── deleteProduto ────────────────────────────────────────────────────────────

describe('deleteProduto', () => {
  test('retorna 404 se produto não encontrado', async () => {
    const req = { params: { id: '99' } };
    const res = mockRes();
    mockConn.query.mockResolvedValueOnce({ rows: [] });

    await deleteProduto(req, res);

    expect(res.status).toHaveBeenCalledWith(404);
    expect(res.json).toHaveBeenCalledWith({ error: 'Produto não encontrado' });
  });

  test('deleta produto com sucesso', async () => {
    const req = { params: { id: '1' } };
    const res = mockRes();
    mockConn.query.mockResolvedValueOnce({ rows: [{ id: 1 }] });

    await deleteProduto(req, res);

    expect(res.json).toHaveBeenCalledWith({ message: 'Produto deletado com sucesso' });
    expect(mockConn.release).toHaveBeenCalled();
  });
});
