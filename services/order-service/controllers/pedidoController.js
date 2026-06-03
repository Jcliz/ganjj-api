const db = require('../../shared/db');
const { decrementStock, restoreStock } = require('../http/productClient');

async function createPedido(req, res) {
  const usuario_id = req.usuario.id;
  const { itens } = req.body;

  if (!Array.isArray(itens) || itens.length === 0) {
    return res.status(400).json({ error: 'Itens do pedido são obrigatórios' });
  }

  for (const item of itens) {
    if (!item.produto_id || !item.quantidade || item.preco == null) {
      return res.status(400).json({ error: 'Cada item deve ter produto_id, quantidade e preco' });
    }
  }

  const decrementados = [];

  try {
    // Decrementa estoque de cada item via product-service (comunicação HTTP)
    for (const item of itens) {
      try {
        await decrementStock(item.produto_id, item.quantidade);
        decrementados.push(item);
      } catch (err) {
        // Compensa (saga): restaura estoque dos itens já decrementados
        for (const d of decrementados) {
          await restoreStock(d.produto_id, d.quantidade).catch(() => {});
        }
        if (err.status === 404) return res.status(404).json({ error: `Produto ${item.produto_id} não encontrado` });
        return res.status(400).json({ error: err.message || 'Estoque insuficiente', estoque_disponivel: err.estoque_disponivel });
      }
    }

    // Todos os estoques decrementados — salva o pedido no banco
    const total = itens.reduce((sum, item) => sum + item.preco * item.quantidade, 0);
    const conn = await db.connect();
    try {
      const compraResult = await conn.query(
        `INSERT INTO compra (usuario_id, total, status) VALUES ($1, $2, 'pending')
         RETURNING id, status, total, criado_em`,
        [usuario_id, total.toFixed(2)]
      );

      const compra = compraResult.rows[0];
      for (const item of itens) {
        await conn.query(
          `INSERT INTO compra_itens (compra_id, produto_id, quantidade, preco) VALUES ($1, $2, $3, $4)`,
          [compra.id, item.produto_id, item.quantidade, item.preco]
        );
      }

      res.status(201).json({
        id: compra.id,
        codigo: `#GNJ-${String(compra.id).padStart(5, '0')}`,
        status: compra.status,
        total: parseFloat(compra.total),
        criado_em: compra.criado_em,
      });
    } catch (dbErr) {
      // Falha ao salvar pedido — compensa o estoque
      for (const d of decrementados) {
        await restoreStock(d.produto_id, d.quantidade).catch(() => {});
      }
      throw dbErr;
    } finally {
      conn.release();
    }
  } catch (error) {
    console.error('Erro ao criar pedido:', error);
    if (!res.headersSent) res.status(500).json({ error: 'Erro ao criar pedido' });
  }
}

async function getPedido(req, res) {
  const { id } = req.params;
  const conn = await db.connect();
  try {
    const compraResult = await conn.query(
      `SELECT c.id, c.total, c.status, c.criado_em, u.nome AS cliente, u.email
       FROM compra c LEFT JOIN usuario u ON c.usuario_id = u.id WHERE c.id = $1`,
      [id]
    );
    if (compraResult.rows.length === 0) return res.status(404).json({ error: 'Pedido não encontrado' });

    const itensResult = await conn.query(
      `SELECT ci.quantidade, ci.preco, p.nome, p.imagem_url
       FROM compra_itens ci JOIN produto p ON ci.produto_id = p.id WHERE ci.compra_id = $1`,
      [id]
    );

    const compra = compraResult.rows[0];
    res.json({
      id: compra.id,
      codigo: `#GNJ-${String(compra.id).padStart(5, '0')}`,
      cliente: compra.cliente, email: compra.email,
      status: compra.status, total: parseFloat(compra.total), criado_em: compra.criado_em,
      itens: itensResult.rows.map(r => ({
        nome: r.nome, quantidade: r.quantidade, preco: parseFloat(r.preco), imagem_url: r.imagem_url,
      })),
    });
  } catch (error) {
    console.error('Erro ao buscar pedido:', error);
    res.status(500).json({ error: 'Erro ao buscar pedido' });
  } finally {
    conn.release();
  }
}

module.exports = { createPedido, getPedido };
