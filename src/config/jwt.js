const jwt = require('jsonwebtoken');

const JWT_SECRET          = process.env.JWT_SECRET;
const JWT_EXPIRES_IN      = process.env.JWT_EXPIRES_IN      || '15m';
const REFRESH_SECRET      = process.env.JWT_REFRESH_SECRET;
const REFRESH_EXPIRES_IN  = process.env.JWT_REFRESH_EXPIRES_IN || '7d';

if (!JWT_SECRET || !REFRESH_SECRET) {
    throw new Error('JWT_SECRET e JWT_REFRESH_SECRET precisam estar definidos no .env');
}

const cookieOptions = {
    httpOnly: true,
    secure: process.env.NODE_ENV === 'production',
    sameSite: 'strict',
    maxAge: 15 * 60 * 1000, //15 minutos em ms
};

const refreshCookieOptions = {
    httpOnly: true,
    secure: process.env.NODE_ENV === 'production',
    sameSite: 'strict',
    maxAge: 7 * 24 * 60 * 60 * 1000, //7 dias em ms
    path: '/api/auth/refresh',
};

function gerarToken(payload) {
    return jwt.sign(payload, JWT_SECRET, { expiresIn: JWT_EXPIRES_IN });
}

function gerarRefreshToken(payload) {
    return jwt.sign(payload, REFRESH_SECRET, { expiresIn: REFRESH_EXPIRES_IN });
}

function verificarToken(token) {
    return jwt.verify(token, JWT_SECRET);
}

function verificarRefreshToken(token) {
    return jwt.verify(token, REFRESH_SECRET);
}

module.exports = {
    gerarToken,
    gerarRefreshToken,
    verificarToken,
    verificarRefreshToken,
    cookieOptions,
    refreshCookieOptions,
};
