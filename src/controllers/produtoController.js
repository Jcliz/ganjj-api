const db = require('../../db/db');

function parseBoolean(value) {
    if (value === undefined) {
        return undefined;
    }

    if (typeof value === 'boolean') {
        return value;
    }

    if (typeof value === 'string') {
        const normalized = value.trim().toLowerCase();
        if (normalized === 'true' || normalized === '1' || normalized === 'active') {
            return true;
        }
        if (normalized === 'false' || normalized === '0' || normalized === 'inactive') {
            return false;
        }
    }

    return undefined;
}

function parsePreco(value) {
    if (value === undefined) {
        return undefined;
    }

    const parsed = Number(value);
    if (!Number.isFinite(parsed) || parsed < 0) {
        return undefined;
    }

    return parsed;
}

function parseEstoque(value) {
    if (value === undefined) {
        return undefined;
    }

    const parsed = Number(value);
    if (!Number.isInteger(parsed) || parsed < 0) {
        return undefined;
    }

    return parsed;
}

function mapProdutoResponse(row) {
    return {
        id: row.id,
        nome: row.nome,
        descricao: row.descricao,
        preco: Number(row.preco),
        estoque: row.estoque,
        cor: row.cor,
        status: row.status,
        imagem_url: row.imagem_url,
        popular: row.popular,
        feminino: row.feminino,
        criado_em: row.criado_em
    };
}

async function createProduto(req, res) {
    try {
        const {
            nome,
            descricao,
            preco,
            estoque,
            cor,
            status,
            imagem_url,
            popular,
            feminino
        } = req.body;

        if (!nome || preco === undefined || estoque === undefined) {
            return res.status(400).json({
                error: 'Campos obrigatórios: nome, preco, estoque'
            });
        }

        const precoParsed = parsePreco(preco);
        if (precoParsed === undefined) {
            return res.status(400).json({ error: 'Preço inválido' });
        }

        const estoqueParsed = parseEstoque(estoque);
        if (estoqueParsed === undefined) {
            return res.status(400).json({ error: 'Estoque inválido' });
        }

        const statusParsed = parseBoolean(status);
        if (status !== undefined && statusParsed === undefined) {
            return res.status(400).json({ error: 'Status inválido' });
        }

        const popularParsed = parseBoolean(popular);
        if (popular !== undefined && popularParsed === undefined) {
            return res.status(400).json({ error: 'Valor inválido para popular' });
        }

        const femininoParsed = parseBoolean(feminino);
        if (feminino !== undefined && femininoParsed === undefined) {
            return res.status(400).json({ error: 'Valor inválido para feminino' });
        }

        const conn = await db.connect();
        try {
            const result = await conn.query(
                `INSERT INTO produto (nome, descricao, preco, estoque, cor, status, imagem_url, popular, feminino)
                 VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9)
                 RETURNING *`,
                [
                    nome,
                    descricao || null,
                    precoParsed,
                    estoqueParsed,
                    cor || null,
                    statusParsed ?? true,
                    imagem_url || null,
                    popularParsed ?? false,
                    femininoParsed ?? false
                ]
            );

            res.status(201).json(mapProdutoResponse(result.rows[0]));
        } finally {
            conn.release();
        }
    } catch (error) {
        console.error('Erro ao criar produto:', error);
        res.status(500).json({ error: 'Erro ao criar produto' });
    }
}

async function getProdutos(req, res) {
    try {
        const conn = await db.connect();
        try {
            const result = await conn.query('SELECT * FROM produto ORDER BY criado_em DESC');
            res.json(result.rows.map(mapProdutoResponse));
        } finally {
            conn.release();
        }
    } catch (error) {
        console.error('Erro ao buscar produtos:', error);
        res.status(500).json({ error: 'Erro ao buscar produtos' });
    }
}

async function getProdutoById(req, res) {
    try {
        const { id } = req.params;

        const conn = await db.connect();
        try {
            const result = await conn.query('SELECT * FROM produto WHERE id = $1', [id]);

            if (result.rows.length === 0) {
                return res.status(404).json({ error: 'Produto não encontrado' });
            }

            res.json(mapProdutoResponse(result.rows[0]));
        } finally {
            conn.release();
        }
    } catch (error) {
        console.error('Erro ao buscar produto:', error);
        res.status(500).json({ error: 'Erro ao buscar produto' });
    }
}

async function updateProduto(req, res) {
    try {
        const { id } = req.params;
        const {
            nome,
            descricao,
            preco,
            estoque,
            cor,
            status,
            imagem_url,
            popular,
            feminino
        } = req.body;

        const updates = [];
        const values = [];
        let index = 1;

        if (nome !== undefined) {
            updates.push(`nome = $${index++}`);
            values.push(nome);
        }

        if (descricao !== undefined) {
            updates.push(`descricao = $${index++}`);
            values.push(descricao || null);
        }

        if (preco !== undefined) {
            const precoParsed = parsePreco(preco);
            if (precoParsed === undefined) {
                return res.status(400).json({ error: 'Preço inválido' });
            }
            updates.push(`preco = $${index++}`);
            values.push(precoParsed);
        }

        if (estoque !== undefined) {
            const estoqueParsed = parseEstoque(estoque);
            if (estoqueParsed === undefined) {
                return res.status(400).json({ error: 'Estoque inválido' });
            }
            updates.push(`estoque = $${index++}`);
            values.push(estoqueParsed);
        }

        if (cor !== undefined) {
            updates.push(`cor = $${index++}`);
            values.push(cor || null);
        }

        if (status !== undefined) {
            const statusParsed = parseBoolean(status);
            if (statusParsed === undefined) {
                return res.status(400).json({ error: 'Status inválido' });
            }
            updates.push(`status = $${index++}`);
            values.push(statusParsed);
        }

        if (imagem_url !== undefined) {
            updates.push(`imagem_url = $${index++}`);
            values.push(imagem_url || null);
        }

        if (popular !== undefined) {
            const popularParsed = parseBoolean(popular);
            if (popularParsed === undefined) {
                return res.status(400).json({ error: 'Valor inválido para popular' });
            }
            updates.push(`popular = $${index++}`);
            values.push(popularParsed);
        }

        if (feminino !== undefined) {
            const femininoParsed = parseBoolean(feminino);
            if (femininoParsed === undefined) {
                return res.status(400).json({ error: 'Valor inválido para feminino' });
            }
            updates.push(`feminino = $${index++}`);
            values.push(femininoParsed);
        }

        if (updates.length === 0) {
            return res.status(400).json({ error: 'Nenhum campo para atualizar' });
        }

        values.push(id);

        const conn = await db.connect();
        try {
            const result = await conn.query(
                `UPDATE produto
                 SET ${updates.join(', ')}
                 WHERE id = $${index}
                 RETURNING *`,
                values
            );

            if (result.rows.length === 0) {
                return res.status(404).json({ error: 'Produto não encontrado' });
            }

            res.json(mapProdutoResponse(result.rows[0]));
        } finally {
            conn.release();
        }
    } catch (error) {
        console.error('Erro ao atualizar produto:', error);
        res.status(500).json({ error: 'Erro ao atualizar produto' });
    }
}

async function deleteProduto(req, res) {
    try {
        const { id } = req.params;

        const conn = await db.connect();
        try {
            const result = await conn.query(
                'DELETE FROM produto WHERE id = $1 RETURNING id',
                [id]
            );

            if (result.rows.length === 0) {
                return res.status(404).json({ error: 'Produto não encontrado' });
            }

            res.json({ message: 'Produto deletado com sucesso' });
        } finally {
            conn.release();
        }
    } catch (error) {
        console.error('Erro ao deletar produto:', error);
        res.status(500).json({ error: 'Erro ao deletar produto' });
    }
}

module.exports = {
    createProduto,
    getProdutos,
    getProdutoById,
    updateProduto,
    deleteProduto,
};
