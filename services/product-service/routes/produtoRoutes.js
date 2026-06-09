const express = require('express');
const router = express.Router();
const produtoController = require('../controllers/produtoController');
const uploadController  = require('../controllers/uploadController');
const upload            = require('../middleware/upload');
const { verificarAutenticacao, verificarAdmin } = require('../../shared/authMiddleware');

// Leitura — público
router.get('/',    produtoController.getProdutos);
router.get('/:id', produtoController.getProdutoById);

// Upload de imagem — admin apenas (deve vir antes de /:id)
router.post('/upload', verificarAutenticacao, verificarAdmin, upload.single('imagem'), uploadController.uploadImagem);

// Escrita — admin apenas
router.post('/',      verificarAutenticacao, verificarAdmin, produtoController.createProduto);
router.put('/:id',    verificarAutenticacao, verificarAdmin, produtoController.updateProduto);
router.delete('/:id', verificarAutenticacao, verificarAdmin, produtoController.deleteProduto);

module.exports = router;
