const express = require('express');
const router = express.Router();
const { createPedido, getPedido, listarMeusPedidos, listarTodosPedidos, atualizarPasso } = require('../controllers/pedidoController');
const { verificarAutenticacao, verificarAdmin } = require('../../shared/authMiddleware');

router.post('/',             verificarAutenticacao,               createPedido);
router.get('/meus',          verificarAutenticacao,               listarMeusPedidos);
router.get('/admin/todos',   verificarAutenticacao, verificarAdmin, listarTodosPedidos);
router.put('/:id/passo',     verificarAutenticacao, verificarAdmin, atualizarPasso);
router.get('/:id',           verificarAutenticacao,               getPedido);

module.exports = router;
