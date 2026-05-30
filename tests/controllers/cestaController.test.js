jest.mock('../../db/db');

const db = require('../../db/db');
const {
  getCesta,
  adicionarItem,
  atualizarItem,
  removerItem,
  limparCesta,
} = require('../../src/controllers/cestaController');

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
      .mockResolvedValueOnce({ rows: [{ id: 5 }] }) // SELECT cesta
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

  test('retorna 404 se produto não encontrado ou inativo', async () => {
    const req = { usuario: { id: 1 }, body: { produto_id: 99, quantidade: 1 } };
    const res = mockRes();
    mockConn.query.mockResolvedValueOnce({ rows: [] });

    await adicionarItem(req, res);

    expect(res.status).toHaveBeenCalledWith(404);
    expect(res.json).toHaveBeenCalledWith({ error: 'Produto não encontrado' });
  });

  test('retorna 400 se estoque insuficiente', async () => {
    const req = { usuario: { id: 1 }, body: { produto_id: 1, quantidade: 10 } };
    const res = mockRes();
    mockConn.query.mockResolvedValueOnce({ rows: [{ estoque: 3 }] });

    await adicionarItem(req, res);

    expect(res.status).toHaveBeenCalledWith(400);
    expect(res.json).toHaveBeenCalledWith({ error: 'Estoque insuficiente' });
  });

  test('adiciona item com sucesso e retorna 201', async () => {
    const req = { usuario: { id: 1 }, body: { produto_id: 1, quantidade: 2 } };
    const res = mockRes();
    mockConn.query
      .mockResolvedValueOnce({ rows: [{ estoque: 10 }] }) // SELECT produto
      .mockResolvedValueOnce({ rows: [{ id: 5 }] })       // INSERT/UPSERT cesta
      .mockResolvedValueOnce({});                          // INSERT/UPSERT cesta_itens

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
      .mockResolvedValueOnce({ rows: [{ id: 5 }] }) // SELECT cesta
      .mockResolvedValueOnce({ rows: [] });          // UPDATE cesta_itens (não encontrou)

    await atualizarItem(req, res);

    expect(res.status).toHaveBeenCalledWith(404);
    expect(res.json).toHaveBeenCalledWith({ error: 'Item não encontrado na cesta' });
  });

  test('atualiza quantidade do item com sucesso', async () => {
    const req = { usuario: { id: 1 }, params: { produto_id: '1' }, body: { quantidade: 3 } };
    const res = mockRes();
    mockConn.query
      .mockResolvedValueOnce({ rows: [{ id: 5 }] })   // SELECT cesta
      .mockResolvedValueOnce({ rows: [{ id: 10 }] }); // UPDATE cesta_itens

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
      .mockResolvedValueOnce({ rows: [{ id: 5 }] }) // SELECT cesta
      .mockResolvedValueOnce({});                    // DELETE cesta_itens

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
    mockConn.query.mockResolvedValueOnce({});

    await limparCesta(req, res);

    expect(res.json).toHaveBeenCalledWith({ message: 'Cesta limpa' });
    expect(mockConn.release).toHaveBeenCalled();
  });
});
