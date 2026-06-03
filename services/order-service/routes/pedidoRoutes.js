const express = require('express');
const router = express.Router();
const { createPedido, getPedido } = require('../controllers/pedidoController');
const { verificarAutenticacao } = require('../../shared/authMiddleware');

router.post('/',   verificarAutenticacao, createPedido);
router.get('/:id', verificarAutenticacao, getPedido);

module.exports = router;
