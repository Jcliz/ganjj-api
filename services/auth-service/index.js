const path = require('path');
require('dotenv').config({ path: path.join(__dirname, '.env') });
const express = require('express');
const cookieParser = require('cookie-parser');

const app = express();
app.use(express.json());
app.use(cookieParser());

const authRoutes     = require('./routes/authRoutes');
const clienteRoutes  = require('./routes/clienteRoutes');

app.use('/api/auth',     authRoutes);
app.use('/api/clientes', clienteRoutes);

const PORT = process.env.PORT || 3001;
app.listen(PORT, () => console.log(`[auth-service] rodando em :${PORT}`));
