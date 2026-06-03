const db = require('../../shared/db');

function parseBoolean(value) {
    if (value === undefined) return undefined;
    if (typeof value === 'boolean') return value;
    if (typeof value === 'string') {
        const n = value.trim().toLowerCase();
        if (n === 'true'  || n === '1' || n === 'active')   return true;
        if (n === 'false' || n === '0' || n === 'inactive') return false;
    }
    return undefined;
}

function parsePreco(value) {
    if (value === undefined) return undefined;
    const parsed = Number(value);
    if (!Number.isFinite(parsed) || parsed < 0) return undefined;
    return parsed;
}

function parseEstoque(value) {
    if (value === undefined) return undefined;
    const parsed = Number(value);
    if (!Number.isInteger(parsed) || parsed < 0) return undefined;
    return parsed;
}

function mapProdutoResponse(row) {
    return {
        id:           row.id,
        nome:         row.nome,
        descricao:    row.descricao,
        preco:        Number(row.preco),
        estoque:      row.estoque,
        cor:          row.cor,
        status:       row.status,
        imagem_url:   row.imagem_url,
        popular:      row.popular,
        feminino:     row.feminino,
        novo:         row.novo   ?? false,
        social:       row.social ?? false,
        tipo_roupa:   row.tipo_roupa  ?? null,
        tamanhos:     row.tamanhos && row.tamanhos.length > 0 ? row.tamanhos : null,
        criado_em:    row.criado_em,
        em_sale:      row.em_sale     ?? false,
        desconto_pct: row.desconto_pct ?? null,
        preco_sale:   row.preco_sale  ? Number(row.preco_sale) : null,
    };
}

async function resolverTipoRoupaId(conn, nome) {
    if (!nome) return null;
    const r = await conn.query('SELECT id FROM tipo_roupa WHERE nome ILIKE $1', [nome]);
    return r.rows.length > 0 ? r.rows[0].id : null;
}

async function salvarTamanhos(conn, produto_id, tamanhos) {
    await conn.query('DELETE FROM produto_tamanhos WHERE produto_id = $1', [produto_id]);
    if (!Array.isArray(tamanhos) || tamanhos.length === 0) return;
    for (const tam of tamanhos) {
        await conn.query(
            'INSERT INTO produto_tamanhos (produto_id, tamanho) VALUES ($1, $2) ON CONFLICT DO NOTHING',
            [produto_id, tam]
        );
    }
}

const SELECT_PRODUTO = `
    SELECT p.*,
           tr.nome AS tipo_roupa,
           (SELECT COALESCE(array_agg(pt.tamanho ORDER BY pt.tamanho), ARRAY[]::TEXT[])
            FROM produto_tamanhos pt WHERE pt.produto_id = p.id) AS tamanhos,
           s.desconto_pct,
           (s.id IS NOT NULL) AS em_sale,
           CASE WHEN s.id IS NOT NULL
                THEN ROUND(p.preco * (1 - s.desconto_pct::numeric / 100), 2)
                ELSE NULL
           END AS preco_sale
    FROM produto p
    LEFT JOIN tipo_roupa tr ON tr.id = p.tipo_roupa_id
    LEFT JOIN sale s ON s.produto_id = p.id AND s.ativo = TRUE
`;

async function getProdutos(req, res) {
    try {
        const { feminino, tipo_roupa, tamanho, popular, preco_max, novo, social } = req.query;
        const conditions = ['p.status = TRUE'];
        const values = [];
        let idx = 1;

        if (feminino    !== undefined) { const v = parseBoolean(feminino);    if (v !== undefined) { conditions.push(`p.feminino = $${idx++}`);  values.push(v); } }
        if (tipo_roupa)                {                                         conditions.push(`tr.nome ILIKE $${idx++}`);                       values.push(tipo_roupa); }
        if (tamanho)                   {                                         conditions.push(`EXISTS (SELECT 1 FROM produto_tamanhos pt WHERE pt.produto_id = p.id AND pt.tamanho = $${idx++})`); values.push(tamanho); }
        if (popular     !== undefined) { const v = parseBoolean(popular);     if (v !== undefined) { conditions.push(`p.popular = $${idx++}`);   values.push(v); } }
        if (novo        !== undefined) { const v = parseBoolean(novo);        if (v !== undefined) { conditions.push(`p.novo = $${idx++}`);      values.push(v); } }
        if (social      !== undefined) { const v = parseBoolean(social);      if (v !== undefined) { conditions.push(`p.social = $${idx++}`);    values.push(v); } }
        if (preco_max   !== undefined) { const v = parsePreco(preco_max);     if (v !== undefined) { conditions.push(`p.preco <= $${idx++}`);    values.push(v); } }

        const conn = await db.connect();
        try {
            const result = await conn.query(
                `${SELECT_PRODUTO} WHERE ${conditions.join(' AND ')} ORDER BY p.criado_em DESC`,
                values
            );
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
            const result = await conn.query(`${SELECT_PRODUTO} WHERE p.id = $1`, [id]);
            if (result.rows.length === 0) return res.status(404).json({ error: 'Produto não encontrado' });
            res.json(mapProdutoResponse(result.rows[0]));
        } finally {
            conn.release();
        }
    } catch (error) {
        console.error('Erro ao buscar produto:', error);
        res.status(500).json({ error: 'Erro ao buscar produto' });
    }
}

async function createProduto(req, res) {
    try {
        const { nome, descricao, preco, estoque, cor, status, imagem_url, popular, feminino, novo, social, tipo_roupa, tamanhos } = req.body;

        if (!nome || preco === undefined || estoque === undefined)
            return res.status(400).json({ error: 'Campos obrigatórios: nome, preco, estoque' });

        const precoParsed   = parsePreco(preco);
        const estoqueParsed = parseEstoque(estoque);
        if (precoParsed   === undefined) return res.status(400).json({ error: 'Preço inválido' });
        if (estoqueParsed === undefined) return res.status(400).json({ error: 'Estoque inválido' });

        const conn = await db.connect();
        try {
            const tipo_roupa_id = await resolverTipoRoupaId(conn, tipo_roupa);
            const result = await conn.query(
                `INSERT INTO produto (nome, descricao, preco, estoque, cor, status, imagem_url, popular, feminino, novo, social, tipo_roupa_id)
                 VALUES ($1,$2,$3,$4,$5,$6,$7,$8,$9,$10,$11,$12) RETURNING *`,
                [nome, descricao || null, precoParsed, estoqueParsed, cor || null,
                 parseBoolean(status) ?? true, imagem_url || null, parseBoolean(popular) ?? false,
                 parseBoolean(feminino) ?? false, parseBoolean(novo) ?? true, parseBoolean(social) ?? false, tipo_roupa_id]
            );
            await salvarTamanhos(conn, result.rows[0].id, tamanhos);
            const full = await conn.query(`${SELECT_PRODUTO} WHERE p.id = $1`, [result.rows[0].id]);
            res.status(201).json(mapProdutoResponse(full.rows[0]));
        } finally {
            conn.release();
        }
    } catch (error) {
        console.error('Erro ao criar produto:', error);
        res.status(500).json({ error: 'Erro ao criar produto' });
    }
}

async function updateProduto(req, res) {
    try {
        const { id } = req.params;
        const { nome, descricao, preco, estoque, cor, status, imagem_url, popular, feminino, novo, social, tipo_roupa, tamanhos } = req.body;

        const updates = [];
        const values  = [];
        let index = 1;

        if (nome      !== undefined) { updates.push(`nome = $${index++}`);      values.push(nome); }
        if (descricao !== undefined) { updates.push(`descricao = $${index++}`); values.push(descricao || null); }
        if (preco     !== undefined) { const p = parsePreco(preco);     if (p === undefined) return res.status(400).json({ error: 'Preço inválido' });            updates.push(`preco = $${index++}`);     values.push(p); }
        if (estoque   !== undefined) { const e = parseEstoque(estoque); if (e === undefined) return res.status(400).json({ error: 'Estoque inválido' });          updates.push(`estoque = $${index++}`);   values.push(e); }
        if (cor        !== undefined) { updates.push(`cor = $${index++}`);        values.push(cor || null); }
        if (imagem_url !== undefined) { updates.push(`imagem_url = $${index++}`); values.push(imagem_url || null); }
        if (status  !== undefined) { const v = parseBoolean(status);  if (v === undefined) return res.status(400).json({ error: 'Status inválido' });              updates.push(`status = $${index++}`);   values.push(v); }
        if (popular !== undefined) { const v = parseBoolean(popular); if (v === undefined) return res.status(400).json({ error: 'Valor inválido para popular' });  updates.push(`popular = $${index++}`);  values.push(v); }
        if (feminino!== undefined) { const v = parseBoolean(feminino);if (v === undefined) return res.status(400).json({ error: 'Valor inválido para feminino' }); updates.push(`feminino = $${index++}`); values.push(v); }
        if (novo    !== undefined) { const v = parseBoolean(novo);    if (v === undefined) return res.status(400).json({ error: 'Valor inválido para novo' });     updates.push(`novo = $${index++}`);     values.push(v); }
        if (social  !== undefined) { const v = parseBoolean(social);  if (v === undefined) return res.status(400).json({ error: 'Valor inválido para social' });   updates.push(`social = $${index++}`);   values.push(v); }

        const conn = await db.connect();
        try {
            if (tipo_roupa !== undefined) {
                const tipo_id = await resolverTipoRoupaId(conn, tipo_roupa);
                updates.push(`tipo_roupa_id = $${index++}`);
                values.push(tipo_id);
            }

            if (updates.length === 0 && tamanhos === undefined)
                return res.status(400).json({ error: 'Nenhum campo para atualizar' });

            if (updates.length > 0) {
                values.push(id);
                const result = await conn.query(
                    `UPDATE produto SET ${updates.join(', ')} WHERE id = $${index} RETURNING *`,
                    values
                );
                if (result.rows.length === 0) return res.status(404).json({ error: 'Produto não encontrado' });
            }

            if (tamanhos !== undefined) await salvarTamanhos(conn, id, tamanhos);

            const full = await conn.query(`${SELECT_PRODUTO} WHERE p.id = $1`, [id]);
            if (full.rows.length === 0) return res.status(404).json({ error: 'Produto não encontrado' });
            res.json(mapProdutoResponse(full.rows[0]));
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
            const result = await conn.query('DELETE FROM produto WHERE id = $1 RETURNING id', [id]);
            if (result.rows.length === 0) return res.status(404).json({ error: 'Produto não encontrado' });
            res.json({ message: 'Produto deletado com sucesso' });
        } finally {
            conn.release();
        }
    } catch (error) {
        console.error('Erro ao deletar produto:', error);
        res.status(500).json({ error: 'Erro ao deletar produto' });
    }
}

module.exports = { createProduto, getProdutos, getProdutoById, updateProduto, deleteProduto };
