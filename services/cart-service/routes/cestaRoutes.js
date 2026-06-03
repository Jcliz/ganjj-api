const express = require('express');
const router = express.Router();
const { verificarAutenticacao } = require('../../shared/authMiddleware');
const { getCesta, adicionarItem, atualizarItem, removerItem, limparCesta } = require('../controllers/cestaController');

router.use(verificarAutenticacao);

router.get('/',                     getCesta);
router.post('/itens',               adicionarItem);
router.put('/itens/:produto_id',    atualizarItem);
router.delete('/itens/:produto_id', removerItem);
router.delete('/',                  limparCesta);

module.exports = router;
