require('dotenv').config();

const port = process.env.PORT;
const appUrl = process.env.APP_URL || 'http://localhost:5173';

const express = require('express');
const cookieParser = require('cookie-parser');
const app = express();

// ─── CORS ─────────────────────────────────────────────────────────────────────
// Permite credenciais (cookies) da origem do front-end
app.use((req, res, next) => {
    res.setHeader('Access-Control-Allow-Origin', appUrl);
    res.setHeader('Access-Control-Allow-Methods', 'GET, POST, PUT, DELETE, OPTIONS');
    res.setHeader('Access-Control-Allow-Headers', 'Content-Type, Authorization');
    res.setHeader('Access-Control-Allow-Credentials', 'true');

    if (req.method === 'OPTIONS') {
        return res.status(200).end();
    }

    next();
});

// ─── Parsers ──────────────────────────────────────────────────────────────────
app.use(express.json());
app.use(cookieParser());

// ─── Rotas ────────────────────────────────────────────────────────────────────
const authRouter = require('./routes/auth');
app.use('/api/auth', authRouter);

// ─── Start ────────────────────────────────────────────────────────────────────
app.listen(port, () => {
    console.log(`GANJJ API listening at http://localhost:${port}`);
});
