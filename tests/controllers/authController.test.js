jest.mock('../../db/db');
jest.mock('bcrypt');
jest.mock('../../src/config/jwt', () => ({
  gerarToken: jest.fn(() => 'mock-access-token'),
  gerarRefreshToken: jest.fn(() => 'mock-refresh-token'),
  verificarRefreshToken: jest.fn(),
  cookieOptions: { httpOnly: true },
  refreshCookieOptions: { httpOnly: true },
}));

const db = require('../../db/db');
const bcrypt = require('bcrypt');
const jwtConfig = require('../../src/config/jwt');
const { login, register, logout, refresh, me } = require('../../src/controllers/authController');

function mockRes() {
  const res = {};
  res.status = jest.fn().mockReturnValue(res);
  res.json = jest.fn().mockReturnValue(res);
  res.cookie = jest.fn().mockReturnValue(res);
  res.clearCookie = jest.fn().mockReturnValue(res);
  return res;
}

let mockConn;

beforeEach(() => {
  mockConn = { query: jest.fn(), release: jest.fn() };
  db.connect.mockResolvedValue(mockConn);
});

// ─── login ────────────────────────────────────────────────────────────────────

describe('login', () => {
  test('retorna 400 se email ou senha não fornecidos', async () => {
    const req = { body: { email: 'a@b.com' } };
    const res = mockRes();

    await login(req, res);

    expect(res.status).toHaveBeenCalledWith(400);
    expect(res.json).toHaveBeenCalledWith({ error: 'Email e senha são obrigatórios' });
  });

  test('retorna 401 se usuário não encontrado', async () => {
    const req = { body: { email: 'a@b.com', senha: '123' } };
    const res = mockRes();
    mockConn.query.mockResolvedValueOnce({ rows: [] });

    await login(req, res);

    expect(res.status).toHaveBeenCalledWith(401);
    expect(res.json).toHaveBeenCalledWith({ error: 'Credenciais inválidas' });
  });

  test('retorna 401 se senha incorreta', async () => {
    const req = { body: { email: 'a@b.com', senha: 'errada' } };
    const res = mockRes();
    mockConn.query.mockResolvedValueOnce({
      rows: [{ id: 1, nome: 'João', email: 'a@b.com', senha: 'hash', is_admin: false }],
    });
    bcrypt.compare.mockResolvedValueOnce(false);

    await login(req, res);

    expect(res.status).toHaveBeenCalledWith(401);
    expect(res.json).toHaveBeenCalledWith({ error: 'Credenciais inválidas' });
  });

  test('retorna 200 com tokens em cookies no login bem-sucedido', async () => {
    const req = { body: { email: 'a@b.com', senha: 'correta' } };
    const res = mockRes();
    mockConn.query.mockResolvedValueOnce({
      rows: [{ id: 1, nome: 'João Silva', email: 'a@b.com', senha: 'hash', is_admin: false }],
    });
    bcrypt.compare.mockResolvedValueOnce(true);

    await login(req, res);

    expect(res.cookie).toHaveBeenCalledWith('accessToken', 'mock-access-token', expect.any(Object));
    expect(res.cookie).toHaveBeenCalledWith('refreshToken', 'mock-refresh-token', expect.any(Object));
    expect(res.json).toHaveBeenCalledWith(
      expect.objectContaining({ message: 'Login realizado com sucesso' })
    );
    expect(mockConn.release).toHaveBeenCalled();
  });
});

// ─── logout ───────────────────────────────────────────────────────────────────

describe('logout', () => {
  test('limpa os cookies e retorna mensagem de sucesso', () => {
    const req = {};
    const res = mockRes();

    logout(req, res);

    expect(res.clearCookie).toHaveBeenCalledWith('accessToken', expect.any(Object));
    expect(res.clearCookie).toHaveBeenCalledWith('refreshToken', expect.any(Object));
    expect(res.json).toHaveBeenCalledWith({ message: 'Logout realizado com sucesso' });
  });
});

// ─── refresh ──────────────────────────────────────────────────────────────────

describe('refresh', () => {
  test('retorna 401 se refreshToken não fornecido', () => {
    const req = { cookies: {} };
    const res = mockRes();

    refresh(req, res);

    expect(res.status).toHaveBeenCalledWith(401);
    expect(res.json).toHaveBeenCalledWith({ error: 'Refresh token não fornecido' });
  });

  test('retorna 401 se refreshToken inválido', () => {
    const req = { cookies: { refreshToken: 'token-invalido' } };
    const res = mockRes();
    jwtConfig.verificarRefreshToken.mockImplementationOnce(() => {
      throw new Error('invalid token');
    });

    refresh(req, res);

    expect(res.status).toHaveBeenCalledWith(401);
    expect(res.json).toHaveBeenCalledWith(
      expect.objectContaining({ error: expect.stringContaining('inválido') })
    );
  });

  test('renova o accessToken com refreshToken válido', () => {
    const req = { cookies: { refreshToken: 'token-valido' } };
    const res = mockRes();
    jwtConfig.verificarRefreshToken.mockReturnValueOnce({ id: 1, email: 'a@b.com', isAdmin: false });

    refresh(req, res);

    expect(res.cookie).toHaveBeenCalledWith('accessToken', 'mock-access-token', expect.any(Object));
    expect(res.json).toHaveBeenCalledWith({ message: 'Token renovado com sucesso' });
  });
});

// ─── register ─────────────────────────────────────────────────────────────────

describe('register', () => {
  test('retorna 400 se campos obrigatórios faltam', async () => {
    const req = { body: { firstName: 'João', email: 'a@b.com' } };
    const res = mockRes();

    await register(req, res);

    expect(res.status).toHaveBeenCalledWith(400);
    expect(res.json).toHaveBeenCalledWith({ error: 'Todos os campos são obrigatórios' });
  });

  test('retorna 409 se email já cadastrado', async () => {
    const req = { body: { firstName: 'João', lastName: 'Silva', email: 'a@b.com', password: '123' } };
    const res = mockRes();
    bcrypt.hash.mockResolvedValueOnce('hash');
    mockConn.query.mockResolvedValueOnce({ rows: [{ id: 1 }] });

    await register(req, res);

    expect(res.status).toHaveBeenCalledWith(409);
    expect(res.json).toHaveBeenCalledWith({ error: 'E-mail já cadastrado' });
  });

  test('cria usuário e retorna 201 com tokens em cookies', async () => {
    const req = { body: { firstName: 'João', lastName: 'Silva', email: 'novo@b.com', password: '123' } };
    const res = mockRes();
    bcrypt.hash.mockResolvedValueOnce('hash');
    mockConn.query
      .mockResolvedValueOnce({ rows: [] })
      .mockResolvedValueOnce({
        rows: [{ id: 2, nome: 'João Silva', email: 'novo@b.com', is_admin: false, criado_em: new Date() }],
      });

    await register(req, res);

    expect(res.status).toHaveBeenCalledWith(201);
    expect(res.cookie).toHaveBeenCalledWith('accessToken', 'mock-access-token', expect.any(Object));
    expect(res.cookie).toHaveBeenCalledWith('refreshToken', 'mock-refresh-token', expect.any(Object));
    expect(mockConn.release).toHaveBeenCalled();
  });
});

// ─── me ───────────────────────────────────────────────────────────────────────

describe('me', () => {
  test('retorna 401 se req.usuario não definido', async () => {
    const req = { usuario: null };
    const res = mockRes();

    await me(req, res);

    expect(res.status).toHaveBeenCalledWith(401);
    expect(res.json).toHaveBeenCalledWith({ error: 'Não autenticado' });
  });

  test('retorna 404 se usuário não encontrado no banco', async () => {
    const req = { usuario: { id: 99 } };
    const res = mockRes();
    mockConn.query.mockResolvedValueOnce({ rows: [] });

    await me(req, res);

    expect(res.status).toHaveBeenCalledWith(404);
    expect(res.json).toHaveBeenCalledWith({ error: 'Usuário não encontrado' });
  });

  test('retorna dados do usuário autenticado', async () => {
    const req = { usuario: { id: 1 } };
    const res = mockRes();
    const usuario = { id: 1, nome: 'João', email: 'a@b.com', is_admin: false, criado_em: new Date() };
    mockConn.query.mockResolvedValueOnce({ rows: [usuario] });

    await me(req, res);

    expect(res.json).toHaveBeenCalledWith({ usuario });
    expect(mockConn.release).toHaveBeenCalled();
  });
});
