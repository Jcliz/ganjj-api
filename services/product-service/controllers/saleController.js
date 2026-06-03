const db = require('../../shared/db');

async function listarSale(req, res) {
  try {
    const { categoria } = req.query;
    const params = [];
    let whereCategoria = '';
    if (categoria && categoria !== 'Todos') {
      params.push(categoria);
      whereCategoria = `AND s.categoria = $${params.length}`;
    }

    const query = `
      SELECT p.id, p.nome, p.descricao, p.preco, p.estoque, p.cor, p.status,
             p.imagem_url, p.popular, p.feminino, p.criado_em,
             s.id AS sale_id, s.desconto_pct, s.categoria,
             ROUND(p.preco * (1 - s.desconto_pct::numeric / 100), 2) AS preco_sale
      FROM sale s
      JOIN produto p ON p.id = s.produto_id
      WHERE s.ativo = TRUE AND p.status = TRUE ${whereCategoria}
      ORDER BY s.desconto_pct DESC, p.nome ASC
    `;

    const conn = await db.connect();
    try {
      const result = await conn.query(query, params);
      res.json(result.rows.map(row => ({
        id: row.id, nome: row.nome, descricao: row.descricao,
        preco: Number(row.preco), preco_sale: Number(row.preco_sale),
        estoque: row.estoque, cor: row.cor, status: row.status,
        imagem_url: row.imagem_url, popular: row.popular, feminino: row.feminino,
        criado_em: row.criado_em, sale_id: row.sale_id,
        desconto_pct: row.desconto_pct, categoria: row.categoria,
      })));
    } finally {
      conn.release();
    }
  } catch (error) {
    console.error('Erro ao buscar produtos em sale:', error);
    res.status(500).json({ error: 'Erro ao buscar produtos em sale' });
  }
}

module.exports = { listarSale };
