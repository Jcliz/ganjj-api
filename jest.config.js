module.exports = {
  testEnvironment: 'node',
  testMatch: ['**/services/**/tests/**/*.test.js'],
  setupFiles: ['./tests/setup.js'],
  clearMocks: true,
  collectCoverageFrom: [
    'services/auth-service/controllers/**/*.js',
    'services/cart-service/controllers/**/*.js',
    'services/order-service/controllers/**/*.js',
    'services/product-service/controllers/produtoController.js',
    'services/shared/authMiddleware.js',
  ],
};
