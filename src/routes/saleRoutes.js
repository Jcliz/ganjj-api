const express = require('express');
const router  = express.Router();
const { listarSale } = require('../controllers/saleController');

router.get('/', listarSale);

module.exports = router;
