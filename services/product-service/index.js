const path = require('path');
require('dotenv').config({ path: path.join(__dirname, '.env') });
const express = require('express');
const cookieParser = require('cookie-parser');

const app = express();
app.use(express.json());
app.use(cookieParser());

const produtoRoutes  = require('./routes/produtoRoutes');
const saleRoutes     = require('./routes/saleRoutes');
const stockRoutes    = require('./routes/stockRoutes');

app.use('/api/produtos',  produtoRoutes);
app.use('/api/sale',      saleRoutes);
app.use('/internal',      stockRoutes);

const PORT = process.env.PORT || 3002;
app.listen(PORT, () => console.log(`[product-service] rodando em :${PORT}`));
