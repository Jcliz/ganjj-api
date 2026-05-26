const express = require('express');
const router = express.Router();
const clienteController = require('../controllers/clienteController');
//const { verificarAutenticacao, verificarAdmin } = require('../middleware/authMiddleware'); verificacao ADMIN

// router.use(verificarAutenticacao);
// router.use(verificarAdmin);

router.post('/', clienteController.createCliente);

router.get('/', clienteController.getClientes);

router.get('/:id', clienteController.getClienteById);

router.put('/:id', clienteController.updateCliente);

router.delete('/:id', clienteController.deleteCliente);

module.exports = router;