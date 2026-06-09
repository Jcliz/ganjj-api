const express = require('express');
const router = express.Router();
const dashboardController = require('../controllers/dashboardController');
const { verificarAutenticacao, verificarAdmin } = require('../../shared/authMiddleware');

// Admin apenas
router.get('/', verificarAutenticacao, verificarAdmin, dashboardController.getDashboard);

module.exports = router;
