module.exports = {
  testEnvironment: 'node',
  testMatch: ['**/services/**/tests/**/*.test.js'],
  setupFiles: ['./tests/setup.js'],
  clearMocks: true,
  collectCoverageFrom: [
    'services/auth-service/controllers/**/*.js',
    'services/cart-service/controllers/**/*.js',
    'services/order-service/controllers/**/*.js',
    'services/product-service/controllers/**/*.js',
    'services/dashboard-service/controllers/**/*.js',
    'services/shared/authMiddleware.js',
  ],
  coverageThreshold: {
    global: {
      statements: 75,
      lines: 75,
    },
  },
};
