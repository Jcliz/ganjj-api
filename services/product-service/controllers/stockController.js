const db = require('../../shared/db');

function verificarInterno(req, res, next) {
    if (req.headers['x-internal-secret'] !== process.env.INTERNAL_SECRET) {
        return res.status(403).json({ error: 'Acesso não autorizado' });
    }
    next();
}

async function getStock(req, res) {
    const { id } = req.params;
    const conn = await db.connect();
    try {
        const result = await conn.query(
            'SELECT id, nome, estoque FROM produto WHERE id = $1 AND status = true',
            [id]
        );
        if (result.rows.length === 0) return res.status(404).json({ error: `Produto ${id} não encontrado` });
        res.json(result.rows[0]);
    } catch (error) {
        console.error('Erro ao buscar estoque:', error);
        res.status(500).json({ error: 'Erro ao buscar estoque' });
    } finally {
        conn.release();
    }
}

async function decrementStock(req, res) {
    const { id } = req.params;
    const { quantidade } = req.body;

    if (!quantidade || quantidade < 1) {
        return res.status(400).json({ error: 'quantidade deve ser maior que zero' });
    }

    const conn = await db.connect();
    try {
        const result = await conn.query(
            `UPDATE produto SET estoque = estoque - $1
             WHERE id = $2 AND status = true AND estoque >= $1
             RETURNING id, nome, estoque`,
            [quantidade, id]
        );

        if (result.rows.length === 0) {
            const check = await conn.query('SELECT id, estoque FROM produto WHERE id = $1 AND status = true', [id]);
            if (check.rows.length === 0) return res.status(404).json({ error: `Produto ${id} não encontrado` });
            return res.status(400).json({ error: 'Estoque insuficiente', estoque_disponivel: check.rows[0].estoque });
        }

        res.json({ ok: true, id: result.rows[0].id, estoque: result.rows[0].estoque });
    } catch (error) {
        console.error('Erro ao decrementar estoque:', error);
        res.status(500).json({ error: 'Erro ao decrementar estoque' });
    } finally {
        conn.release();
    }
}

async function restoreStock(req, res) {
    const { id } = req.params;
    const { quantidade } = req.body;

    if (!quantidade || quantidade < 1) {
        return res.status(400).json({ error: 'quantidade deve ser maior que zero' });
    }

    const conn = await db.connect();
    try {
        await conn.query('UPDATE produto SET estoque = estoque + $1 WHERE id = $2', [quantidade, id]);
        res.json({ ok: true });
    } catch (error) {
        console.error('Erro ao restaurar estoque:', error);
        res.status(500).json({ error: 'Erro ao restaurar estoque' });
    } finally {
        conn.release();
    }
}

module.exports = { verificarInterno, getStock, decrementStock, restoreStock };
