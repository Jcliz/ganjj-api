require('dotenv').config();

const port = process.env.PORT;
const appUrl = process.env.APP_URL || 'http://localhost:5173';

const { runSetup } = require('./db/setup');
runSetup().catch(err => console.error('Erro no DB setup:', err));

const express      = require('express');
const cookieParser = require('cookie-parser');
const { apiReference } = require('@scalar/express-api-reference');
const openApiSpec      = require('./src/docs/openapi');
const app = express();

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

const authRoutes      = require('./src/routes/authRoutes');
const clienteRoutes   = require('./src/routes/clienteRoutes');
const produtoRoutes   = require('./src/routes/produtoRoutes');
const dashboardRoutes = require('./src/routes/dashboardRoutes');
const pedidoRoutes    = require('./src/routes/pedidoRoutes');
const cestaRoutes     = require('./src/routes/cestaRoutes');

app.get('/api/docs.json', (req, res) => res.json(openApiSpec));
app.use('/api/docs', apiReference({
    spec: { content: openApiSpec },
    theme: 'purple',
}));

app.use('/api/auth',      authRoutes);

//rotas protegidas — descomente verificarAutenticacao em cada arquivo de rota
app.use('/api/clientes',  clienteRoutes);
app.use('/api/produtos',  produtoRoutes);
app.use('/api/dashboard', dashboardRoutes);
app.use('/api/pedidos',   pedidoRoutes);
app.use('/api/cesta',     cestaRoutes);

app.listen(port, () => {
    console.log(`GANJJ API listening at http://localhost:${port}`);
});
