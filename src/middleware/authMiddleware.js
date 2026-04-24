const { verificarToken } = require('../config/jwt');

// Aplique em qualquer rota que exija usuário logado:
//   router.use(verificarAutenticacao);          ← protege todas as rotas do arquivo
//   router.get('/:id', verificarAutenticacao, controller.get);  ← protege rota individual
//
// O token pode vir de dois lugares (nessa ordem de prioridade):
//   1. Cookie httpOnly "accessToken"  (fluxo web — preferido)
//   2. Header Authorization: Bearer <token>  (fluxo mobile / API externa)
function verificarAutenticacao(req, res, next) {
    const tokenDoCookie  = req.cookies?.accessToken;
    const authHeader     = req.headers['authorization'];
    const tokenDoHeader  = authHeader?.startsWith('Bearer ') ? authHeader.slice(7) : null;

    const token = tokenDoCookie || tokenDoHeader;

    if (!token) {
        return res.status(401).json({ error: 'Token de autenticação não fornecido' });
    }

    try {
        const payload = verificarToken(token);
        req.usuario = payload; // { id, email, isAdmin, iat, exp }
        next();
    } catch (err) {
        const expirou = err.name === 'TokenExpiredError';
        return res.status(401).json({
            error: expirou ? 'Token expirado' : 'Token inválido',
        });
    }
}

// Aplique DEPOIS de verificarAutenticacao em rotas exclusivas de admin:
//   router.use(verificarAutenticacao, verificarAdmin);
//   router.delete('/:id', verificarAutenticacao, verificarAdmin, controller.delete);
function verificarAdmin(req, res, next) {
    if (!req.usuario?.isAdmin) {
        return res.status(403).json({ error: 'Acesso restrito a administradores' });
    }
    next();
}

module.exports = { verificarAutenticacao, verificarAdmin };
