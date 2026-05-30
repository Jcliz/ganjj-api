jest.mock('../../db/db');

const db = require('../../db/db');
const {
  createCliente,
  getClientes,
  getClienteById,
  updateCliente,
  deleteCliente,
} = require('../../src/controllers/clienteController');

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

// ─── createCliente ────────────────────────────────────────────────────────────

describe('createCliente', () => {
  test('retorna 400 se campos obrigatórios faltam', async () => {
    const req = { body: { firstName: 'João', email: 'a@b.com' } };
    const res = mockRes();

    await createCliente(req, res);

    expect(res.status).toHaveBeenCalledWith(400);
    expect(res.json).toHaveBeenCalledWith(
      expect.objectContaining({ error: expect.stringContaining('obrigatórios') })
    );
  });

  test('retorna 400 se email inválido', async () => {
    const req = { body: { firstName: 'João', lastName: 'Silva', email: 'email-invalido' } };
    const res = mockRes();

    await createCliente(req, res);

    expect(res.status).toHaveBeenCalledWith(400);
    expect(res.json).toHaveBeenCalledWith({ error: 'Email inválido' });
  });

  test('retorna 409 se email já cadastrado', async () => {
    const req = { body: { firstName: 'João', lastName: 'Silva', email: 'a@b.com' } };
    const res = mockRes();
    mockConn.query.mockResolvedValueOnce({ rows: [{ id: 1 }] });

    await createCliente(req, res);

    expect(res.status).toHaveBeenCalledWith(409);
    expect(res.json).toHaveBeenCalledWith({ error: 'Email já cadastrado' });
  });

  test('cria cliente com sucesso e retorna 201', async () => {
    const req = { body: { firstName: 'João', lastName: 'Silva', email: 'novo@b.com', role: 'Admin', status: 'Active' } };
    const res = mockRes();
    mockConn.query
      .mockResolvedValueOnce({ rows: [] })
      .mockResolvedValueOnce({
        rows: [{ id: 1, nome: 'João Silva', email: 'novo@b.com', isAdmin: true, status: true, criado_em: new Date() }],
      });

    await createCliente(req, res);

    expect(res.status).toHaveBeenCalledWith(201);
    expect(mockConn.release).toHaveBeenCalled();
  });
});

// ─── getClientes ──────────────────────────────────────────────────────────────

describe('getClientes', () => {
  test('retorna lista de clientes', async () => {
    const req = {};
    const res = mockRes();
    mockConn.query.mockResolvedValueOnce({
      rows: [
        { id: 1, nome: 'João Silva', email: 'a@b.com', isAdmin: false, status: true, criado_em: new Date() },
      ],
    });

    await getClientes(req, res);

    expect(res.json).toHaveBeenCalledWith(
      expect.arrayContaining([expect.objectContaining({ email: 'a@b.com' })])
    );
    expect(mockConn.release).toHaveBeenCalled();
  });

  test('retorna lista vazia se não há clientes', async () => {
    const req = {};
    const res = mockRes();
    mockConn.query.mockResolvedValueOnce({ rows: [] });

    await getClientes(req, res);

    expect(res.json).toHaveBeenCalledWith([]);
  });
});

// ─── getClienteById ───────────────────────────────────────────────────────────

describe('getClienteById', () => {
  test('retorna 404 se cliente não encontrado', async () => {
    const req = { params: { id: '99' } };
    const res = mockRes();
    mockConn.query.mockResolvedValueOnce({ rows: [] });

    await getClienteById(req, res);

    expect(res.status).toHaveBeenCalledWith(404);
    expect(res.json).toHaveBeenCalledWith({ error: 'Usuário não encontrado' });
  });

  test('retorna cliente pelo id', async () => {
    const req = { params: { id: '1' } };
    const res = mockRes();
    mockConn.query.mockResolvedValueOnce({
      rows: [{ id: 1, nome: 'João Silva', email: 'a@b.com', isAdmin: false, status: true, criado_em: new Date() }],
    });

    await getClienteById(req, res);

    expect(res.json).toHaveBeenCalledWith(
      expect.objectContaining({ id: 1, email: 'a@b.com' })
    );
  });
});

// ─── updateCliente ────────────────────────────────────────────────────────────

describe('updateCliente', () => {
  test('retorna 400 se nenhum campo enviado', async () => {
    const req = { params: { id: '1' }, body: {} };
    const res = mockRes();

    await updateCliente(req, res);

    expect(res.status).toHaveBeenCalledWith(400);
    expect(res.json).toHaveBeenCalledWith({ error: 'Nenhum campo para atualizar' });
  });

  test('retorna 400 se email inválido', async () => {
    const req = { params: { id: '1' }, body: { email: 'invalido' } };
    const res = mockRes();

    await updateCliente(req, res);

    expect(res.status).toHaveBeenCalledWith(400);
    expect(res.json).toHaveBeenCalledWith({ error: 'Email inválido' });
  });

  test('retorna 409 se email já em uso por outro usuário', async () => {
    const req = { params: { id: '1' }, body: { email: 'outro@b.com' } };
    const res = mockRes();
    mockConn.query.mockResolvedValueOnce({ rows: [{ id: 2 }] });

    await updateCliente(req, res);

    expect(res.status).toHaveBeenCalledWith(409);
    expect(res.json).toHaveBeenCalledWith({ error: 'Email já cadastrado' });
  });

  test('atualiza cliente com sucesso', async () => {
    const req = { params: { id: '1' }, body: { email: 'novo@b.com' } };
    const res = mockRes();
    mockConn.query
      .mockResolvedValueOnce({ rows: [] })
      .mockResolvedValueOnce({
        rows: [{ id: 1, nome: 'João Silva', email: 'novo@b.com', isAdmin: false, status: true, criado_em: new Date() }],
      });

    await updateCliente(req, res);

    expect(res.json).toHaveBeenCalledWith(
      expect.objectContaining({ email: 'novo@b.com' })
    );
    expect(mockConn.release).toHaveBeenCalled();
  });

  test('retorna 404 se cliente não encontrado ao atualizar', async () => {
    const req = { params: { id: '99' }, body: { email: 'x@b.com' } };
    const res = mockRes();
    mockConn.query
      .mockResolvedValueOnce({ rows: [] })
      .mockResolvedValueOnce({ rows: [] });

    await updateCliente(req, res);

    expect(res.status).toHaveBeenCalledWith(404);
    expect(res.json).toHaveBeenCalledWith({ error: 'Usuário não encontrado' });
  });
});

// ─── deleteCliente ────────────────────────────────────────────────────────────

describe('deleteCliente', () => {
  test('retorna 404 se cliente não encontrado', async () => {
    const req = { params: { id: '99' } };
    const res = mockRes();
    mockConn.query.mockResolvedValueOnce({ rows: [] });

    await deleteCliente(req, res);

    expect(res.status).toHaveBeenCalledWith(404);
    expect(res.json).toHaveBeenCalledWith({ error: 'Usuário não encontrado' });
  });

  test('deleta cliente com sucesso', async () => {
    const req = { params: { id: '1' } };
    const res = mockRes();
    mockConn.query.mockResolvedValueOnce({ rows: [{ id: 1 }] });

    await deleteCliente(req, res);

    expect(res.json).toHaveBeenCalledWith({ message: 'Usuário deletado com sucesso' });
    expect(mockConn.release).toHaveBeenCalled();
  });
});
