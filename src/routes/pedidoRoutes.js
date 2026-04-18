const express = require('express');
const router = express.Router();
const { createPedido, getPedido } = require('../controllers/pedidoController');

router.post('/', createPedido);
router.get('/:id', getPedido);

module.exports = router;
