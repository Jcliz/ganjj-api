const express = require('express');
const router = express.Router();
const produtoController = require('../controllers/produtoController');
//const { verificarAutenticacao, verificarAdmin } = require('../middleware/authMiddleware'); verificacao ADMIN

// router.use(verificarAutenticacao);
// router.use(verificarAdmin);

router.post('/', produtoController.createProduto);

router.get('/', produtoController.getProdutos);

router.get('/:id', produtoController.getProdutoById);

router.put('/:id', produtoController.updateProduto);

router.delete('/:id', produtoController.deleteProduto);

module.exports = router;