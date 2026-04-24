const express = require('express');
const router = express.Router();
const authController = require('../controllers/authController');

//gera accessToken + refreshToken (cookies httpOnly)
router.post('/login', authController.login);

//limpa os cookies de sessão
router.post('/logout', authController.logout);

//troca o refreshToken por um novo accessToken
router.post('/refresh', authController.refresh);

module.exports = router;
