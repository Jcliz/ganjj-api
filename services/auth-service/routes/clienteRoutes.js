const express = require('express');
const router = express.Router();
const clienteController = require('../controllers/clienteController');
const { verificarAutenticacao, verificarAdmin } = require('../../shared/authMiddleware');

// Admin apenas
router.use(verificarAutenticacao, verificarAdmin);

router.post('/',      clienteController.createCliente);
router.get('/',       clienteController.getClientes);
router.get('/:id',    clienteController.getClienteById);
router.put('/:id',    clienteController.updateCliente);
router.delete('/:id', clienteController.deleteCliente);

module.exports = router;
