const path = require('path');
require('dotenv').config({ path: path.join(__dirname, '.env') });
const express = require('express');
const cookieParser = require('cookie-parser');

const app = express();
app.use(express.json());
app.use(cookieParser());

const pedidoRoutes = require('./routes/pedidoRoutes');

app.use('/api/pedidos', pedidoRoutes);

const PORT = process.env.PORT || 3004;
app.listen(PORT, () => console.log(`[order-service] rodando em :${PORT}`));
