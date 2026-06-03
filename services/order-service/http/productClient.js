const PRODUCT_SERVICE_URL = process.env.PRODUCT_SERVICE_URL || 'http://localhost:3002';
const INTERNAL_SECRET = process.env.INTERNAL_SECRET;

async function decrementStock(produtoId, quantidade) {
    const response = await fetch(`${PRODUCT_SERVICE_URL}/internal/produtos/${produtoId}/decrement-stock`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', 'x-internal-secret': INTERNAL_SECRET },
        body: JSON.stringify({ quantidade }),
    });

    if (!response.ok) {
        const body = await response.json().catch(() => ({}));
        const err = new Error(body.error || 'Erro no product-service');
        err.status = response.status;
        err.estoque_disponivel = body.estoque_disponivel;
        throw err;
    }

    return response.json();
}

async function restoreStock(produtoId, quantidade) {
    await fetch(`${PRODUCT_SERVICE_URL}/internal/produtos/${produtoId}/restore-stock`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', 'x-internal-secret': INTERNAL_SECRET },
        body: JSON.stringify({ quantidade }),
    });
}

module.exports = { decrementStock, restoreStock };
