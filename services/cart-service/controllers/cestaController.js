const db = require('../../shared/db');
const productClient = require('../http/productClient');

async function getOrCreateCarrinho(conn, usuarioId) {
  const result = await conn.query('SELECT id FROM carrinho WHERE usuario_id = $1', [usuarioId]);
  if (result.rows.length > 0) return result.rows[0].id;
  const inserted = await conn.query(
    'INSERT INTO carrinho (usuario_id) VALUES ($1) RETURNING id',
    [usuarioId]
  );
  return inserted.rows[0].id;
}

async function getCesta(req, res) {
  const usuarioId = req.usuario.id;
  let conn;
  try {
    conn = await db.connect();
    const carrinhoResult = await conn.query('SELECT id FROM carrinho WHERE usuario_id = $1', [usuarioId]);
    if (carrinhoResult.rows.length === 0) return res.json({ itens: [] });

    const carrinhoId = carrinhoResult.rows[0].id;
    const itensResult = await conn.query(
      `SELECT ci.id, ci.produto_id, ci.quantidade, p.nome, p.preco, p.cor, p.imagem_url
       FROM carrinho_itens ci JOIN produto p ON ci.produto_id = p.id
       WHERE ci.carrinho_id = $1 ORDER BY ci.id`,
      [carrinhoId]
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
    if (conn) conn.release();
  }
}

async function adicionarItem(req, res) {
  const usuarioId = req.usuario.id;
  const { produto_id, quantidade = 1 } = req.body;

  if (!produto_id || quantidade < 1) {
    return res.status(400).json({ error: 'produto_id e quantidade são obrigatórios' });
  }

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

  let conn;
  try {
    conn = await db.connect();
    const carrinhoId = await getOrCreateCarrinho(conn, usuarioId);

    const existing = await conn.query(
      'SELECT id, quantidade FROM carrinho_itens WHERE carrinho_id = $1 AND produto_id = $2',
      [carrinhoId, produto_id]
    );

    if (existing.rows.length > 0) {
      await conn.query(
        'UPDATE carrinho_itens SET quantidade = quantidade + $1 WHERE id = $2',
        [quantidade, existing.rows[0].id]
      );
    } else {
      await conn.query(
        'INSERT INTO carrinho_itens (carrinho_id, produto_id, quantidade) VALUES ($1, $2, $3)',
        [carrinhoId, produto_id, quantidade]
      );
    }

    res.status(201).json({ message: 'Item adicionado à cesta' });
  } catch (error) {
    console.error('Erro ao adicionar item:', error);
    res.status(500).json({ error: 'Erro ao adicionar item à cesta' });
  } finally {
    if (conn) conn.release();
  }
}

async function atualizarItem(req, res) {
  const usuarioId = req.usuario.id;
  const { produto_id } = req.params;
  const { quantidade } = req.body;

  if (!quantidade || quantidade < 1) {
    return res.status(400).json({ error: 'Quantidade deve ser maior que zero' });
  }

  let conn;
  try {
    conn = await db.connect();
    const carrinhoResult = await conn.query('SELECT id FROM carrinho WHERE usuario_id = $1', [usuarioId]);
    if (carrinhoResult.rows.length === 0) return res.status(404).json({ error: 'Cesta não encontrada' });

    const result = await conn.query(
      'UPDATE carrinho_itens SET quantidade = $1 WHERE carrinho_id = $2 AND produto_id = $3 RETURNING id',
      [quantidade, carrinhoResult.rows[0].id, produto_id]
    );
    if (result.rows.length === 0) return res.status(404).json({ error: 'Item não encontrado na cesta' });

    res.json({ message: 'Quantidade atualizada' });
  } catch (error) {
    console.error('Erro ao atualizar item:', error);
    res.status(500).json({ error: 'Erro ao atualizar item da cesta' });
  } finally {
    if (conn) conn.release();
  }
}

async function removerItem(req, res) {
  const usuarioId = req.usuario.id;
  const { produto_id } = req.params;

  let conn;
  try {
    conn = await db.connect();
    const carrinhoResult = await conn.query('SELECT id FROM carrinho WHERE usuario_id = $1', [usuarioId]);
    if (carrinhoResult.rows.length === 0) return res.status(404).json({ error: 'Cesta não encontrada' });

    await conn.query(
      'DELETE FROM carrinho_itens WHERE carrinho_id = $1 AND produto_id = $2',
      [carrinhoResult.rows[0].id, produto_id]
    );
    res.json({ message: 'Item removido da cesta' });
  } catch (error) {
    console.error('Erro ao remover item:', error);
    res.status(500).json({ error: 'Erro ao remover item da cesta' });
  } finally {
    if (conn) conn.release();
  }
}

async function limparCesta(req, res) {
  const usuarioId = req.usuario.id;
  let conn;
  try {
    conn = await db.connect();
    const carrinhoResult = await conn.query('SELECT id FROM carrinho WHERE usuario_id = $1', [usuarioId]);
    if (carrinhoResult.rows.length === 0) return res.json({ message: 'Cesta já está vazia' });

    await conn.query('DELETE FROM carrinho_itens WHERE carrinho_id = $1', [carrinhoResult.rows[0].id]);
    res.json({ message: 'Cesta limpa' });
  } catch (error) {
    console.error('Erro ao limpar cesta:', error);
    res.status(500).json({ error: 'Erro ao limpar cesta' });
  } finally {
    if (conn) conn.release();
  }
}

module.exports = { getCesta, adicionarItem, atualizarItem, removerItem, limparCesta };
