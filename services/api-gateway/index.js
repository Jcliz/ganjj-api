const path = require('path');
require('dotenv').config({ path: path.join(__dirname, '.env') });
const http = require('http');
const express = require('express');
const { apiReference } = require('@scalar/express-api-reference');
const openApiSpec = require('./docs/openapi');

const app = express();

const APP_URL = process.env.APP_URL || 'http://localhost:5173';

const ROUTES = [
    { prefix: '/api/auth',      host: 'localhost', port: 3001 },
    { prefix: '/api/clientes',  host: 'localhost', port: 3001 },
    { prefix: '/api/produtos',  host: 'localhost', port: 3002 },
    { prefix: '/api/sale',      host: 'localhost', port: 3002 },
    { prefix: '/api/cesta',     host: 'localhost', port: 3003 },
    { prefix: '/api/pedidos',   host: 'localhost', port: 3004 },
    { prefix: '/api/dashboard', host: 'localhost', port: 3005 },
];

app.use((req, res, next) => {
    res.setHeader('Access-Control-Allow-Origin', APP_URL);
    res.setHeader('Access-Control-Allow-Credentials', 'true');
    res.setHeader('Access-Control-Allow-Methods', 'GET, POST, PUT, DELETE, OPTIONS, PATCH');
    res.setHeader('Access-Control-Allow-Headers', 'Content-Type, Authorization');
    if (req.method === 'OPTIONS') return res.status(204).end();
    next();
});

app.get('/api/docs.json', (req, res) => res.json(openApiSpec));
app.use('/api/docs', apiReference({ spec: { content: openApiSpec }, theme: 'purple' }));

app.use((req, res) => {
    const route = ROUTES.find(r => req.path.startsWith(r.prefix));
    if (!route) return res.status(404).json({ error: 'Rota não encontrada' });

    const options = {
        hostname: route.host,
        port: route.port,
        path: req.originalUrl,
        method: req.method,
        headers: { ...req.headers, host: `${route.host}:${route.port}` },
    };

    const proxy = http.request(options, (proxyRes) => {
        for (const [key, value] of Object.entries(proxyRes.headers)) {
            res.setHeader(key, value);
        }
        res.setHeader('Access-Control-Allow-Origin', APP_URL);
        res.setHeader('Access-Control-Allow-Credentials', 'true');
        res.statusCode = proxyRes.statusCode;
        proxyRes.pipe(res, { end: true });
    });

    proxy.on('error', (err) => {
        console.error(`[gateway] erro ao chamar :${route.port} —`, err.message);
        if (!res.headersSent) res.status(502).json({ error: 'Serviço indisponível' });
    });

    req.pipe(proxy, { end: true });
});

const PORT = process.env.PORT || 3000;
app.listen(PORT, () => console.log(`[api-gateway] rodando em :${PORT}`));
