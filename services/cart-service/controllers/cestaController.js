const db = require('../../shared/db');
const productClient = require('../http/productClient');

async function getCesta(req, res) {
  const usuarioId = req.usuario.id;
  const conn = await db.connect();
  try {
    const cestaResult = await conn.query('SELECT id FROM cesta WHERE usuario_id = $1', [usuarioId]);
    if (cestaResult.rows.length === 0) return res.json({ itens: [] });

    const cestaId = cestaResult.rows[0].id;
    const itensResult = await conn.query(
      `SELECT ci.id, ci.produto_id, ci.quantidade, p.nome, p.preco, p.cor, p.imagem_url
       FROM cesta_itens ci JOIN produto p ON ci.produto_id = p.id
       WHERE ci.cesta_id = $1 ORDER BY ci.added_at`,
      [cestaId]
    );

    res.json({
      itens: itensResult.rows.map(r => ({
        id: r.id, produto_id: r.produto_id, nome: r.nome,
        preco: parseFloat(r.preco), cor: r.cor, imagem_url: r.imagem_url, quantidade: r.quantidade,
      })),
    });
  } catch (error) {
    console.error('Erro ao buscar cesta:', error);
    res.status(500).json({ error: 'Erro ao buscar cesta' });
  } finally {
    conn.release();
  }
}

async function adicionarItem(req, res) {
  const usuarioId = req.usuario.id;
  const { produto_id, quantidade = 1 } = req.body;

  if (!produto_id || quantidade < 1) {
    return res.status(400).json({ error: 'produto_id e quantidade são obrigatórios' });
  }

  // Verifica disponibilidade de estoque via product-service (HTTP)
  try {
    const produto = await productClient.checkStock(produto_id);
    if (produto.estoque < quantidade) {
      return res.status(400).json({ error: 'Estoque insuficiente' });
    }
  } catch (err) {
    if (err.status === 404) return res.status(404).json({ error: 'Produto não encontrado' });
    console.error('Erro ao verificar estoque no product-service:', err);
    return res.status(500).json({ error: 'Erro ao verificar estoque' });
  }

  const conn = await db.connect();
  try {
    const cestaResult = await conn.query(
      `INSERT INTO cesta (usuario_id) VALUES ($1)
       ON CONFLICT (usuario_id) DO UPDATE SET usuario_id = EXCLUDED.usuario_id
       RETURNING id`,
      [usuarioId]
    );
    const cestaId = cestaResult.rows[0].id;

    await conn.query(
      `INSERT INTO cesta_itens (cesta_id, produto_id, quantidade) VALUES ($1, $2, $3)
       ON CONFLICT (cesta_id, produto_id) DO UPDATE SET quantidade = cesta_itens.quantidade + $3`,
      [cestaId, produto_id, quantidade]
    );

    res.status(201).json({ message: 'Item adicionado à cesta' });
  } catch (error) {
    console.error('Erro ao adicionar item:', error);
    res.status(500).json({ error: 'Erro ao adicionar item à cesta' });
  } finally {
    conn.release();
  }
}

async function atualizarItem(req, res) {
  const usuarioId = req.usuario.id;
  const { produto_id } = req.params;
  const { quantidade } = req.body;

  if (!quantidade || quantidade < 1) {
    return res.status(400).json({ error: 'Quantidade deve ser maior que zero' });
  }

  const conn = await db.connect();
  try {
    const cestaResult = await conn.query('SELECT id FROM cesta WHERE usuario_id = $1', [usuarioId]);
    if (cestaResult.rows.length === 0) return res.status(404).json({ error: 'Cesta não encontrada' });

    const result = await conn.query(
      'UPDATE cesta_itens SET quantidade = $1 WHERE cesta_id = $2 AND produto_id = $3 RETURNING id',
      [quantidade, cestaResult.rows[0].id, produto_id]
    );
    if (result.rows.length === 0) return res.status(404).json({ error: 'Item não encontrado na cesta' });

    res.json({ message: 'Quantidade atualizada' });
  } catch (error) {
    console.error('Erro ao atualizar item:', error);
    res.status(500).json({ error: 'Erro ao atualizar item da cesta' });
  } finally {
    conn.release();
  }
}

async function removerItem(req, res) {
  const usuarioId = req.usuario.id;
  const { produto_id } = req.params;

  const conn = await db.connect();
  try {
    const cestaResult = await conn.query('SELECT id FROM cesta WHERE usuario_id = $1', [usuarioId]);
    if (cestaResult.rows.length === 0) return res.status(404).json({ error: 'Cesta não encontrada' });

    await conn.query(
      'DELETE FROM cesta_itens WHERE cesta_id = $1 AND produto_id = $2',
      [cestaResult.rows[0].id, produto_id]
    );
    res.json({ message: 'Item removido da cesta' });
  } catch (error) {
    console.error('Erro ao remover item:', error);
    res.status(500).json({ error: 'Erro ao remover item da cesta' });
  } finally {
    conn.release();
  }
}

async function limparCesta(req, res) {
  const usuarioId = req.usuario.id;
  const conn = await db.connect();
  try {
    await conn.query('DELETE FROM cesta WHERE usuario_id = $1', [usuarioId]);
    res.json({ message: 'Cesta limpa' });
  } catch (error) {
    console.error('Erro ao limpar cesta:', error);
    res.status(500).json({ error: 'Erro ao limpar cesta' });
  } finally {
    conn.release();
  }
}

module.exports = { getCesta, adicionarItem, atualizarItem, removerItem, limparCesta };
