const express = require('express');
const router = express.Router();
const authController = require('../controllers/authController');
const { verificarAutenticacao } = require('../../shared/authMiddleware');

router.post('/register', authController.register);
router.post('/login',    authController.login);
router.post('/logout',   authController.logout);
router.post('/refresh',  authController.refresh);
router.get('/me', verificarAutenticacao, authController.me);

module.exports = router;
