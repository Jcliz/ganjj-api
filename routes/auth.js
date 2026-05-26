const express = require('express');
const bcrypt = require('bcryptjs');
const jwt = require('jsonwebtoken');
const { connect } = require('../db/db');

const router = express.Router();

const COOKIE_NAME = 'ganjj_token';
const COOKIE_OPTIONS = {
    httpOnly: true,
    secure: process.env.NODE_ENV === 'production',
    sameSite: 'lax',
    maxAge: 7 * 24 * 60 * 60 * 1000, // 7 dias
};

function signToken(payload) {
    return jwt.sign(payload, process.env.JWT_SECRET, { expiresIn: '7d' });
}

function verifyToken(token) {
    return jwt.verify(token, process.env.JWT_SECRET);
}

// ─────────────────────────────────────────────
// POST /api/auth/register
// Body: { firstName, lastName, email, password }
// ─────────────────────────────────────────────
router.post('/register', async (req, res) => {
    const { firstName, lastName, email, password } = req.body;

    // Validação básica
    if (!firstName || !lastName || !email || !password) {
        return res.status(400).json({ error: 'Preencha todos os campos.' });
    }
    if (!/\S+@\S+\.\S+/.test(email)) {
        return res.status(400).json({ error: 'E-mail inválido.' });
    }
    if (password.length < 8) {
        return res.status(400).json({ error: 'A senha deve ter pelo menos 8 caracteres.' });
    }

    const nome = `${firstName.trim()} ${lastName.trim()}`;

    try {
        const client = await connect();

        // Verificar se e-mail já existe
        const existing = await client.query(
            'SELECT id FROM usuario WHERE email = $1',
            [email.toLowerCase().trim()]
        );
        if (existing.rows.length > 0) {
            client.release();
            return res.status(409).json({ error: 'Este e-mail já está cadastrado.' });
        }

        // Hash da senha
        const senhaHash = await bcrypt.hash(password, 12);

        // Inserir usuário
        const result = await client.query(
            `INSERT INTO usuario (nome, email, senha)
             VALUES ($1, $2, $3)
             RETURNING id, nome, email, is_admin, criado_em`,
            [nome, email.toLowerCase().trim(), senhaHash]
        );
        client.release();

        const usuario = result.rows[0];
        const token = signToken({ id: usuario.id, email: usuario.email, is_admin: usuario.is_admin });

        res.cookie(COOKIE_NAME, token, COOKIE_OPTIONS);

        return res.status(201).json({
            message: 'Conta criada com sucesso.',
            usuario: {
                id: usuario.id,
                nome: usuario.nome,
                email: usuario.email,
                is_admin: usuario.is_admin,
                criado_em: usuario.criado_em,
            },
        });
    } catch (err) {
        console.error('[register]', err);
        return res.status(500).json({ error: 'Erro interno do servidor.' });
    }
});

// ─────────────────────────────────────────────
// POST /api/auth/login
// Body: { email, password }
// ─────────────────────────────────────────────
router.post('/login', async (req, res) => {
    const { email, password } = req.body;

    if (!email || !password) {
        return res.status(400).json({ error: 'Preencha todos os campos.' });
    }

    try {
        const client = await connect();

        const result = await client.query(
            'SELECT id, nome, email, senha, is_admin, criado_em FROM usuario WHERE email = $1',
            [email.toLowerCase().trim()]
        );
        client.release();

        if (result.rows.length === 0) {
            return res.status(401).json({ error: 'E-mail ou senha incorretos.' });
        }

        const usuario = result.rows[0];

        const senhaValida = await bcrypt.compare(password, usuario.senha);
        if (!senhaValida) {
            return res.status(401).json({ error: 'E-mail ou senha incorretos.' });
        }

        const token = signToken({ id: usuario.id, email: usuario.email, is_admin: usuario.is_admin });

        res.cookie(COOKIE_NAME, token, COOKIE_OPTIONS);

        return res.status(200).json({
            message: 'Login realizado com sucesso.',
            usuario: {
                id: usuario.id,
                nome: usuario.nome,
                email: usuario.email,
                is_admin: usuario.is_admin,
                criado_em: usuario.criado_em,
            },
        });
    } catch (err) {
        console.error('[login]', err);
        return res.status(500).json({ error: 'Erro interno do servidor.' });
    }
});

// ─────────────────────────────────────────────
// GET /api/auth/me
// Lê o cookie JWT e retorna os dados da sessão
// ─────────────────────────────────────────────
router.get('/me', async (req, res) => {
    const token = req.cookies?.[COOKIE_NAME];

    if (!token) {
        return res.status(401).json({ error: 'Não autenticado.' });
    }

    let payload;
    try {
        payload = verifyToken(token);
    } catch {
        res.clearCookie(COOKIE_NAME);
        return res.status(401).json({ error: 'Sessão inválida ou expirada.' });
    }

    try {
        const client = await connect();
        const result = await client.query(
            'SELECT id, nome, email, is_admin, criado_em FROM usuario WHERE id = $1',
            [payload.id]
        );
        client.release();

        if (result.rows.length === 0) {
            res.clearCookie(COOKIE_NAME);
            return res.status(401).json({ error: 'Usuário não encontrado.' });
        }

        const usuario = result.rows[0];
        return res.status(200).json({
            usuario: {
                id: usuario.id,
                nome: usuario.nome,
                email: usuario.email,
                is_admin: usuario.is_admin,
                criado_em: usuario.criado_em,
            },
        });
    } catch (err) {
        console.error('[me]', err);
        return res.status(500).json({ error: 'Erro interno do servidor.' });
    }
});

// ─────────────────────────────────────────────
// POST /api/auth/logout
// Remove o cookie JWT
// ─────────────────────────────────────────────
router.post('/logout', (req, res) => {
    res.clearCookie(COOKIE_NAME, {
        httpOnly: true,
        secure: process.env.NODE_ENV === 'production',
        sameSite: 'lax',
    });
    return res.status(200).json({ message: 'Logout realizado com sucesso.' });
});

module.exports = router;
