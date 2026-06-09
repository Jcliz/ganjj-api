const path = require('path');
require('dotenv').config({ path: path.join(__dirname, '.env') });
const express = require('express');
const cookieParser = require('cookie-parser');

const app = express();
app.use(express.json());
app.use(cookieParser());

const dashboardRoutes = require('./routes/dashboardRoutes');

app.use('/api/dashboard', dashboardRoutes);

const PORT = process.env.PORT || 3005;
app.listen(PORT, () => console.log(`[dashboard-service] rodando em :${PORT}`));
