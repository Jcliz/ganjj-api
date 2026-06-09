const express = require('express');
const router = express.Router();
const { verificarInterno, getStock, decrementStock, restoreStock } = require('../controllers/stockController');

router.use(verificarInterno);

router.get('/produtos/:id/stock',            getStock);
router.post('/produtos/:id/decrement-stock', decrementStock);
router.post('/produtos/:id/restore-stock',   restoreStock);

module.exports = router;
