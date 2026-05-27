const bcrypt = require('bcrypt');
const db = require('../../db/db');
const {
    gerarToken,
    gerarRefreshToken,
    verificarRefreshToken,
    cookieOptions,
    refreshCookieOptions,
} = require('../config/jwt');

async function login(req, res) {
    const { email, senha, password } = req.body;
    const senhaRecebida = senha || password;

    if (!email || !senhaRecebida) {
        return res.status(400).json({ error: 'Email e senha são obrigatórios' });
    }

    const conn = await db.connect();
    try {
        // TODO: ajuste a query conforme a coluna de senha do seu schema
        // (atualmente clienteController usa 'senha_temporaria' — trocar por hash bcrypt)
        const result = await conn.query(
            'SELECT id, nome, email, senha, is_admin FROM usuario WHERE email = $1 AND status = true',
            [email]
        );

        if (result.rows.length === 0) {
            return res.status(401).json({ error: 'Credenciais inválidas' });
        }

        const usuario = result.rows[0];

        // TODO: quando o fluxo de cadastro com senha estiver pronto,
        // substitua a linha abaixo por: await bcrypt.compare(senha, usuario.senha)
        const senhaCorreta = await bcrypt.compare(senhaRecebida, usuario.senha);
        if (!senhaCorreta) {
            return res.status(401).json({ error: 'Credenciais inválidas' });
        }

        const payload = {
            id:      usuario.id,
            email:   usuario.email,
            isAdmin: usuario.is_admin,
        };

        const accessToken  = gerarToken(payload);
        const refreshToken = gerarRefreshToken(payload);

        res.cookie('accessToken',  accessToken,  cookieOptions);
        res.cookie('refreshToken', refreshToken, refreshCookieOptions);

        return res.json({
            message: 'Login realizado com sucesso',
            usuario: { id: usuario.id, nome: usuario.nome, email: usuario.email, is_admin: usuario.is_admin },
        });
    } finally {
        conn.release();
    }
}

function logout(req, res) {
    res.clearCookie('accessToken',  { path: '/' });
    res.clearCookie('refreshToken', { path: '/api/auth/refresh' });
    return res.json({ message: 'Logout realizado com sucesso' });
}

function refresh(req, res) {
    const token = req.cookies?.refreshToken;

    if (!token) {
        return res.status(401).json({ error: 'Refresh token não fornecido' });
    }

    try {
        const payload = verificarRefreshToken(token);

        const novoAccessToken = gerarToken({
            id:      payload.id,
            email:   payload.email,
            isAdmin: payload.isAdmin,
        });

        res.cookie('accessToken', novoAccessToken, cookieOptions);
        return res.json({ message: 'Token renovado com sucesso' });
    } catch (err) {
        res.clearCookie('accessToken');
        res.clearCookie('refreshToken', { path: '/api/auth/refresh' });
        return res.status(401).json({ error: 'Refresh token inválido ou expirado. Faça login novamente.' });
    }
}

async function me(req, res) {
    if (!req.usuario) {
        return res.status(401).json({ error: 'Não autenticado' });
    }
    const conn = await db.connect();
    try {
        const result = await conn.query(
            'SELECT id, nome, email, is_admin, criado_em FROM usuario WHERE id = $1',
            [req.usuario.id]
        );
        if (result.rows.length === 0) {
            return res.status(404).json({ error: 'Usuário não encontrado' });
        }
        return res.json({ usuario: result.rows[0] });
    } finally {
        conn.release();
    }
}

module.exports = { login, logout, refresh, me };
