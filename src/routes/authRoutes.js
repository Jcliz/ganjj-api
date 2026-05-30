const express = require('express');
const router = express.Router();
const authController = require('../controllers/authController');
const { verificarAutenticacao } = require('../middleware/authMiddleware');

router.post('/register', authController.register);

//gera accessToken + refreshToken (cookies httpOnly)
router.post('/login', authController.login);

//limpa os cookies de sessão
router.post('/logout', authController.logout);

//troca o refreshToken por um novo accessToken
router.post('/refresh', authController.refresh);

//retorna dados do usuário autenticado pelo cookie
router.get('/me', verificarAutenticacao, authController.me);

module.exports = router;
