jest.mock('../../shared/db');

const db = require('../../shared/db');
const {
  createProduto,
  getProdutos,
  getProdutoById,
  updateProduto,
  deleteProduto,
} = require('../controllers/produtoController');

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
  novo: false,
  social: false,
  tipo_roupa: null,
  tamanhos: [],
  criado_em: new Date(),
  em_sale: false,
  desconto_pct: null,
  preco_sale: null,
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

  test('cria produto com sucesso e retorna 201', async () => {
    const req = { body: { nome: 'Camiseta', preco: 99.90, estoque: 10, cor: 'azul' } };
    const res = mockRes();
    mockConn.query
      .mockResolvedValueOnce({ rows: [{ id: 1 }] })   // INSERT produto
      .mockResolvedValueOnce({})                        // DELETE produto_tamanhos
      .mockResolvedValueOnce({ rows: [produtoRow] });   // SELECT final

    await createProduto(req, res);

    expect(res.status).toHaveBeenCalledWith(201);
    expect(res.json).toHaveBeenCalledWith(expect.objectContaining({ nome: 'Camiseta' }));
    expect(mockConn.release).toHaveBeenCalled();
  });
});

// ─── getProdutos ──────────────────────────────────────────────────────────────

describe('getProdutos', () => {
  test('retorna lista de produtos', async () => {
    const req = { query: {} };
    const res = mockRes();
    mockConn.query.mockResolvedValueOnce({ rows: [produtoRow] });

    await getProdutos(req, res);

    expect(res.json).toHaveBeenCalledWith(
      expect.arrayContaining([expect.objectContaining({ nome: 'Camiseta' })])
    );
    expect(mockConn.release).toHaveBeenCalled();
  });

  test('retorna lista vazia se não há produtos', async () => {
    const req = { query: {} };
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
    mockConn.query
      .mockResolvedValueOnce({ rows: [{ ...produtoRow, nome: 'Novo Nome', preco: '120.00' }] }) // UPDATE
      .mockResolvedValueOnce({ rows: [{ ...produtoRow, nome: 'Novo Nome', preco: '120.00' }] }); // SELECT final

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

// ─── filtros de getProdutos ───────────────────────────────────────────────────

describe('getProdutos com filtros', () => {
  test('aplica todos os filtros de query suportados', async () => {
    const req = {
      query: {
        feminino: 'true', tipo_roupa: 'Camiseta', tamanho: 'M',
        popular: '1', novo: 'false', social: '0', preco_max: '150',
      },
    };
    const res = mockRes();
    mockConn.query.mockResolvedValueOnce({ rows: [produtoRow] });

    await getProdutos(req, res);

    const [sql, values] = mockConn.query.mock.calls[0];
    expect(sql).toContain('p.feminino = $1');
    expect(sql).toContain('tr.nome ILIKE $2');
    expect(sql).toContain('pt.tamanho = $3');
    expect(sql).toContain('p.popular = $4');
    expect(sql).toContain('p.novo = $5');
    expect(sql).toContain('p.social = $6');
    expect(sql).toContain('p.preco <= $7');
    expect(values).toEqual([true, 'Camiseta', 'M', true, false, false, 150]);
  });

  test('ignora filtros booleanos com valor inválido', async () => {
    const req = { query: { feminino: 'talvez', preco_max: 'caro' } };
    const res = mockRes();
    mockConn.query.mockResolvedValueOnce({ rows: [] });

    await getProdutos(req, res);

    const [, values] = mockConn.query.mock.calls[0];
    expect(values).toEqual([]);
  });
});

// ─── tipo_roupa e tamanhos ────────────────────────────────────────────────────

describe('createProduto com tipo_roupa e tamanhos', () => {
  test('resolve tipo_roupa e salva tamanhos', async () => {
    const req = {
      body: { nome: 'Camiseta', preco: 99.9, estoque: 5, tipo_roupa: 'Camiseta', tamanhos: ['P', 'M'] },
    };
    const res = mockRes();
    mockConn.query
      .mockResolvedValueOnce({ rows: [{ id: 3 }] })                       // SELECT tipo_roupa
      .mockResolvedValueOnce({ rows: [{ ...produtoRow, id: 10 }] })       // INSERT produto
      .mockResolvedValueOnce({})                                          // DELETE tamanhos
      .mockResolvedValueOnce({})                                          // INSERT tamanho P
      .mockResolvedValueOnce({})                                          // INSERT tamanho M
      .mockResolvedValueOnce({ rows: [{ ...produtoRow, id: 10, tamanhos: ['M', 'P'] }] }); // SELECT final

    await createProduto(req, res);

    expect(res.status).toHaveBeenCalledWith(201);
    expect(res.json).toHaveBeenCalledWith(expect.objectContaining({ id: 10, tamanhos: ['M', 'P'] }));
  });
});

describe('updateProduto com tipo_roupa', () => {
  test('atualiza tipo_roupa resolvendo o id', async () => {
    const req = { params: { id: '1' }, body: { tipo_roupa: 'Calça' } };
    const res = mockRes();
    mockConn.query
      .mockResolvedValueOnce({ rows: [{ id: 7 }] })          // SELECT tipo_roupa
      .mockResolvedValueOnce({ rows: [produtoRow] })         // UPDATE produto
      .mockResolvedValueOnce({ rows: [produtoRow] });        // SELECT final

    await updateProduto(req, res);

    expect(res.json).toHaveBeenCalledWith(expect.objectContaining({ id: 1 }));
  });
});

// ─── caminhos de erro 500 ─────────────────────────────────────────────────────

describe('erros de banco (500)', () => {
  test.each([
    ['getProdutos',    () => getProdutos({ query: {} }, mockRes500()),            'Erro ao buscar produtos'],
    ['getProdutoById', () => getProdutoById({ params: { id: '1' } }, mockRes500()), 'Erro ao buscar produto'],
    ['deleteProduto',  () => deleteProduto({ params: { id: '1' } }, mockRes500()),  'Erro ao deletar produto'],
  ])('%s retorna 500 se o banco falha', async (_nome, chamar) => {
    mockConn.query.mockRejectedValueOnce(new Error('DB error'));
    await chamar();
    expect(ultimaRes.status).toHaveBeenCalledWith(500);
  });

  test('createProduto retorna 500 se o banco falha', async () => {
    const req = { body: { nome: 'Camiseta', preco: 10, estoque: 1 } };
    const res = mockRes();
    mockConn.query.mockRejectedValueOnce(new Error('DB error'));

    await createProduto(req, res);

    expect(res.status).toHaveBeenCalledWith(500);
    expect(res.json).toHaveBeenCalledWith({ error: 'Erro ao criar produto' });
  });

  test('updateProduto retorna 500 se o banco falha', async () => {
    const req = { params: { id: '1' }, body: { nome: 'Novo nome' } };
    const res = mockRes();
    mockConn.query.mockRejectedValueOnce(new Error('DB error'));

    await updateProduto(req, res);

    expect(res.status).toHaveBeenCalledWith(500);
    expect(res.json).toHaveBeenCalledWith({ error: 'Erro ao atualizar produto' });
  });
});

let ultimaRes;
function mockRes500() {
  ultimaRes = mockRes();
  return ultimaRes;
}
