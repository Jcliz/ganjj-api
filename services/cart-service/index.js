const path = require('path');
require('dotenv').config({ path: path.join(__dirname, '.env') });
const express = require('express');
const cookieParser = require('cookie-parser');

const app = express();
app.use(express.json());
app.use(cookieParser());

const cestaRoutes = require('./routes/cestaRoutes');

app.use('/api/cesta', cestaRoutes);

const PORT = process.env.PORT || 3003;
app.listen(PORT, () => console.log(`[cart-service] rodando em :${PORT}`));
