const { verificarToken } = require('./jwt');

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
        req.usuario = payload;
        next();
    } catch (err) {
        const expirou = err.name === 'TokenExpiredError';
        return res.status(401).json({
            error: expirou ? 'Token expirado' : 'Token inválido',
        });
    }
}

function verificarAdmin(req, res, next) {
    if (!req.usuario?.isAdmin) {
        return res.status(403).json({ error: 'Acesso restrito a administradores' });
    }
    next();
}

module.exports = { verificarAutenticacao, verificarAdmin };
