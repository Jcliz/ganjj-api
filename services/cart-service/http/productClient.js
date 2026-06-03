const PRODUCT_SERVICE_URL = process.env.PRODUCT_SERVICE_URL || 'http://localhost:3002';
const INTERNAL_SECRET = process.env.INTERNAL_SECRET;

async function checkStock(produtoId) {
    const response = await fetch(`${PRODUCT_SERVICE_URL}/internal/produtos/${produtoId}/stock`, {
        headers: { 'x-internal-secret': INTERNAL_SECRET },
    });

    if (!response.ok) {
        const body = await response.json().catch(() => ({}));
        const err = new Error(body.error || 'Erro no product-service');
        err.status = response.status;
        throw err;
    }

    return response.json();
}

module.exports = { checkStock };
