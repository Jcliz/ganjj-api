const bcrypt = require('bcrypt');
const db = require('../../shared/db');
const {
    gerarToken,
    gerarRefreshToken,
    verificarRefreshToken,
    cookieOptions,
    refreshCookieOptions,
} = require('../../shared/jwt');

async function login(req, res) {
    const { email, senha } = req.body;

    if (!email || !senha) {
        return res.status(400).json({ error: 'Email e senha são obrigatórios' });
    }

    let conn;
    try {
        conn = await db.connect();
        const result = await conn.query(
            'SELECT id, nome, email, senha, is_admin FROM usuario WHERE email = $1 AND status = true',
            [email]
        );

        if (result.rows.length === 0) {
            return res.status(401).json({ error: 'Credenciais inválidas' });
        }

        const usuario = result.rows[0];

        const senhaCorreta = await bcrypt.compare(senha, usuario.senha);
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
            accessToken,
            usuario: { id: usuario.id, nome: usuario.nome, email: usuario.email, is_admin: usuario.is_admin },
        });
    } catch (error) {
        console.error('Erro ao realizar login:', error);
        return res.status(500).json({ error: 'Erro ao realizar login' });
    } finally {
        if (conn) conn.release();
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

async function register(req, res) {
    const { firstName, lastName, email, senha } = req.body;

    if (!firstName || !lastName || !email || !senha) {
        return res.status(400).json({ error: 'Todos os campos são obrigatórios' });
    }

    const nome = `${firstName} ${lastName}`;

    let conn;
    try {
        const senhaHash = await bcrypt.hash(senha, 10);
        conn = await db.connect();
        const existing = await conn.query('SELECT id FROM usuario WHERE email = $1', [email]);
        if (existing.rows.length > 0) {
            return res.status(409).json({ error: 'E-mail já cadastrado' });
        }

        const result = await conn.query(
            'INSERT INTO usuario (nome, email, senha) VALUES ($1, $2, $3) RETURNING id, nome, email, is_admin, criado_em',
            [nome, email, senhaHash]
        );

        const usuario = result.rows[0];
        const payload = { id: usuario.id, email: usuario.email, isAdmin: usuario.is_admin };
        const accessToken = gerarToken(payload);

        res.cookie('accessToken',  accessToken,               cookieOptions);
        res.cookie('refreshToken', gerarRefreshToken(payload), refreshCookieOptions);

        return res.status(201).json({ message: 'Cadastro realizado com sucesso', accessToken, usuario });
    } catch (error) {
        console.error('Erro ao realizar cadastro:', error);
        return res.status(500).json({ error: 'Erro ao realizar cadastro' });
    } finally {
        if (conn) conn.release();
    }
}

async function me(req, res) {
    if (!req.usuario) {
        return res.status(401).json({ error: 'Não autenticado' });
    }
    let conn;
    try {
        conn = await db.connect();
        const result = await conn.query(
            'SELECT id, nome, email, is_admin, criado_em FROM usuario WHERE id = $1',
            [req.usuario.id]
        );
        if (result.rows.length === 0) {
            return res.status(404).json({ error: 'Usuário não encontrado' });
        }
        return res.json({ usuario: result.rows[0] });
    } catch (error) {
        console.error('Erro ao buscar usuário autenticado:', error);
        return res.status(500).json({ error: 'Erro ao buscar dados do usuário' });
    } finally {
        if (conn) conn.release();
    }
}

module.exports = { login, register, logout, refresh, me };
