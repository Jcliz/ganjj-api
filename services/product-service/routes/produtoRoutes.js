const express = require('express');
const router = express.Router();
const produtoController = require('../controllers/produtoController');
const { verificarAutenticacao, verificarAdmin } = require('../../shared/authMiddleware');

// Leitura — público
router.get('/',    produtoController.getProdutos);
router.get('/:id', produtoController.getProdutoById);

// Escrita — admin apenas
router.post('/',      verificarAutenticacao, verificarAdmin, produtoController.createProduto);
router.put('/:id',    verificarAutenticacao, verificarAdmin, produtoController.updateProduto);
router.delete('/:id', verificarAutenticacao, verificarAdmin, produtoController.deleteProduto);

module.exports = router;
