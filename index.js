require('dotenv').config()

const port = process.env.PORT;

const express = require('express');
const app = express();

app.use((req, res, next) => {
    res.setHeader('Access-Control-Allow-Origin', '*');
    res.setHeader('Access-Control-Allow-Methods', 'GET, POST, PUT, DELETE, OPTIONS');
    res.setHeader('Access-Control-Allow-Headers', 'Content-Type, Authorization');

    if (req.method === 'OPTIONS') {
        return res.status(200).end();
    }

    next();
});

app.use(express.json());

const clienteRoutes   = require('./src/routes/clienteRoutes');
const produtoRoutes   = require('./src/routes/produtoRoutes');
const dashboardRoutes = require('./src/routes/dashboardRoutes');
const pedidoRoutes    = require('./src/routes/pedidoRoutes');

app.use('/api/clientes',  clienteRoutes);
app.use('/api/produtos',  produtoRoutes);
app.use('/api/dashboard', dashboardRoutes);
app.use('/api/pedidos',   pedidoRoutes);

app.listen(port, () => {
    console.log(`GANJJ API listening at http://localhost:${port}`);
});