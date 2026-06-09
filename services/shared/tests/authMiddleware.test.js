jest.mock('../jwt', () => ({
  verificarToken: jest.fn(),
}));

const { verificarToken } = require('../jwt');
const { verificarAutenticacao, verificarAdmin } = require('../authMiddleware');

function mockRes() {
  const res = {};
  res.status = jest.fn().mockReturnValue(res);
  res.json = jest.fn().mockReturnValue(res);
  return res;
}

// ─── verificarAutenticacao ────────────────────────────────────────────────────

describe('verificarAutenticacao', () => {
  test('retorna 401 se nenhum token fornecido', () => {
    const req = { cookies: {}, headers: {} };
    const res = mockRes();
    const next = jest.fn();

    verificarAutenticacao(req, res, next);

    expect(res.status).toHaveBeenCalledWith(401);
    expect(res.json).toHaveBeenCalledWith({ error: 'Token de autenticação não fornecido' });
    expect(next).not.toHaveBeenCalled();
  });

  test('retorna 401 se token inválido', () => {
    const req = { cookies: { accessToken: 'token-invalido' }, headers: {} };
    const res = mockRes();
    const next = jest.fn();
    verificarToken.mockImplementationOnce(() => { throw new Error('invalid'); });

    verificarAutenticacao(req, res, next);

    expect(res.status).toHaveBeenCalledWith(401);
    expect(res.json).toHaveBeenCalledWith({ error: 'Token inválido' });
    expect(next).not.toHaveBeenCalled();
  });

  test('retorna 401 com mensagem específica se token expirado', () => {
    const req = { cookies: { accessToken: 'token-expirado' }, headers: {} };
    const res = mockRes();
    const next = jest.fn();
    const expiredError = new Error('jwt expired');
    expiredError.name = 'TokenExpiredError';
    verificarToken.mockImplementationOnce(() => { throw expiredError; });

    verificarAutenticacao(req, res, next);

    expect(res.status).toHaveBeenCalledWith(401);
    expect(res.json).toHaveBeenCalledWith({ error: 'Token expirado' });
  });

  test('autentica via cookie e chama next()', () => {
    const payload = { id: 1, email: 'a@b.com', isAdmin: false };
    const req = { cookies: { accessToken: 'token-valido' }, headers: {} };
    const res = mockRes();
    const next = jest.fn();
    verificarToken.mockReturnValueOnce(payload);

    verificarAutenticacao(req, res, next);

    expect(req.usuario).toEqual(payload);
    expect(next).toHaveBeenCalled();
    expect(res.status).not.toHaveBeenCalled();
  });

  test('autentica via header Authorization Bearer e chama next()', () => {
    const payload = { id: 2, email: 'b@b.com', isAdmin: true };
    const req = {
      cookies: {},
      headers: { authorization: 'Bearer token-via-header' },
    };
    const res = mockRes();
    const next = jest.fn();
    verificarToken.mockReturnValueOnce(payload);

    verificarAutenticacao(req, res, next);

    expect(verificarToken).toHaveBeenCalledWith('token-via-header');
    expect(req.usuario).toEqual(payload);
    expect(next).toHaveBeenCalled();
  });

  test('cookie tem prioridade sobre o header Authorization', () => {
    const payload = { id: 1, email: 'a@b.com', isAdmin: false };
    const req = {
      cookies: { accessToken: 'token-do-cookie' },
      headers: { authorization: 'Bearer token-do-header' },
    };
    const res = mockRes();
    const next = jest.fn();
    verificarToken.mockReturnValueOnce(payload);

    verificarAutenticacao(req, res, next);

    expect(verificarToken).toHaveBeenCalledWith('token-do-cookie');
  });
});

// ─── verificarAdmin ───────────────────────────────────────────────────────────

describe('verificarAdmin', () => {
  test('retorna 403 se req.usuario não definido', () => {
    const req = {};
    const res = mockRes();
    const next = jest.fn();

    verificarAdmin(req, res, next);

    expect(res.status).toHaveBeenCalledWith(403);
    expect(res.json).toHaveBeenCalledWith({ error: 'Acesso restrito a administradores' });
    expect(next).not.toHaveBeenCalled();
  });

  test('retorna 403 se usuário não é admin', () => {
    const req = { usuario: { id: 1, isAdmin: false } };
    const res = mockRes();
    const next = jest.fn();

    verificarAdmin(req, res, next);

    expect(res.status).toHaveBeenCalledWith(403);
    expect(next).not.toHaveBeenCalled();
  });

  test('chama next() se usuário é admin', () => {
    const req = { usuario: { id: 1, isAdmin: true } };
    const res = mockRes();
    const next = jest.fn();

    verificarAdmin(req, res, next);

    expect(next).toHaveBeenCalled();
    expect(res.status).not.toHaveBeenCalled();
  });
});
