const db = require('../../shared/db');

const MESES_PT = ['Jan', 'Fev', 'Mar', 'Abr', 'Mai', 'Jun', 'Jul', 'Ago', 'Set', 'Out', 'Nov', 'Dez'];

const STATUS_MAP = {
  completed:  { label: 'Concluído',        color: '#2a7a3b' },
  delivered:  { label: 'Entregue',         color: '#2a7a3b' },
  shipping:   { label: 'Frete',            color: '#4a7ab5' },
  shipped:    { label: 'Enviado',          color: '#4a7ab5' },
  pending:    { label: 'Em processamento', color: '#f5a623' },
  processing: { label: 'Em processamento', color: '#f5a623' },
  cancelled:  { label: 'Cancelado',        color: '#d0021b' },
  canceled:   { label: 'Cancelado',        color: '#d0021b' },
};

async function getDashboard(req, res) {
  let conn;
  try {
    conn = await db.connect();
    const kpisResult = await conn.query(`
      SELECT
        (SELECT COALESCE(SUM(total), 0)  FROM compra WHERE status NOT IN ('cancelled', 'canceled')) AS receita_total,
        (SELECT COUNT(*)                 FROM compra)                                                AS total_pedidos,
        (SELECT COUNT(*)                 FROM usuario WHERE status = true AND is_admin = false)      AS usuarios_ativos,
        (SELECT COUNT(*)                 FROM produto WHERE status = true)                           AS total_produtos,
        (SELECT COUNT(*)                 FROM produto WHERE status = true AND estoque = 0)           AS sem_estoque,
        (SELECT COALESCE(AVG(total), 0)  FROM compra WHERE status NOT IN ('cancelled', 'canceled')) AS ticket_medio
    `);

    const graficoResult = await conn.query(`
      WITH months AS (
        SELECT generate_series(DATE_TRUNC('month', NOW() - INTERVAL '11 months'), DATE_TRUNC('month', NOW()), '1 month'::interval) AS mes
      ),
      monthly_data AS (
        SELECT DATE_TRUNC('month', criado_em) AS mes, SUM(total) AS receita, COUNT(*) AS pedidos
        FROM compra WHERE criado_em >= DATE_TRUNC('month', NOW() - INTERVAL '11 months')
        GROUP BY DATE_TRUNC('month', criado_em)
      )
      SELECT EXTRACT(MONTH FROM m.mes)::int AS mes_num, COALESCE(md.receita, 0) AS receita, COALESCE(md.pedidos, 0) AS pedidos
      FROM months m LEFT JOIN monthly_data md ON m.mes = md.mes ORDER BY m.mes ASC
    `);

    const statusResult = await conn.query(`SELECT status, COUNT(*) AS quantidade FROM compra GROUP BY status ORDER BY quantidade DESC`);

    const recentesResult = await conn.query(`
      SELECT c.id, u.nome AS cliente, c.total, c.status, c.criado_em,
             (SELECT COUNT(*) FROM compra_itens ci WHERE ci.compra_id = c.id) AS itens
      FROM compra c JOIN usuario u ON c.usuario_id = u.id
      ORDER BY c.criado_em DESC LIMIT 6
    `);

    const estoqueBaixoResult = await conn.query(`
      SELECT nome, estoque, feminino FROM produto WHERE status = true AND estoque <= 50 ORDER BY estoque ASC LIMIT 10
    `);

    const topProdutosResult = await conn.query(`
      SELECT p.nome, COALESCE(SUM(ci.preco * ci.quantidade), 0) AS receita, COUNT(DISTINCT ci.compra_id) AS pedidos
      FROM produto p LEFT JOIN compra_itens ci ON p.id = ci.produto_id
      WHERE p.status = true GROUP BY p.id, p.nome
      HAVING COALESCE(SUM(ci.preco * ci.quantidade), 0) > 0
      ORDER BY receita DESC LIMIT 5
    `);

    const kpi = kpisResult.rows[0];
    const kpis = {
      receita_total:   parseFloat(kpi.receita_total),
      total_pedidos:   parseInt(kpi.total_pedidos),
      usuarios_ativos: parseInt(kpi.usuarios_ativos),
      total_produtos:  parseInt(kpi.total_produtos),
      sem_estoque:     parseInt(kpi.sem_estoque),
      ticket_medio:    parseFloat(kpi.ticket_medio),
    };

    const grafico_mensal = graficoResult.rows.map(row => ({
      mes: MESES_PT[row.mes_num - 1], receita: parseFloat(row.receita), pedidos: parseInt(row.pedidos),
    }));

    const totalCompras = statusResult.rows.reduce((sum, r) => sum + parseInt(r.quantidade), 0);
    const statusAgrupado = {};
    for (const row of statusResult.rows) {
      const mapped = STATUS_MAP[row.status] ?? { label: row.status, color: '#737373' };
      if (!statusAgrupado[mapped.label]) statusAgrupado[mapped.label] = { ...mapped, quantidade: 0 };
      statusAgrupado[mapped.label].quantidade += parseInt(row.quantidade);
    }
    const status_pedidos = Object.values(statusAgrupado).map(s => ({
      label: s.label, value: totalCompras > 0 ? Math.round((s.quantidade / totalCompras) * 100) : 0, color: s.color,
    }));

    const pedidos_recentes = recentesResult.rows.map(row => ({
      id: `#GNJ-${String(row.id).padStart(5, '0')}`, cliente: row.cliente,
      itens: parseInt(row.itens), total: parseFloat(row.total),
      status: STATUS_MAP[row.status]?.label ?? row.status,
      data: new Date(row.criado_em).toLocaleDateString('pt-BR', { day: '2-digit', month: 'short' }),
    }));

    const estoque_baixo = estoqueBaixoResult.rows.map(row => ({
      nome: row.nome, estoque: parseInt(row.estoque), categoria: row.feminino ? 'Fem.' : 'Masc.',
    }));

    const maxReceita = topProdutosResult.rows.length > 0 ? parseFloat(topProdutosResult.rows[0].receita) : 1;
    const top_produtos = topProdutosResult.rows.map(row => ({
      nome: row.nome, receita: parseFloat(row.receita), pedidos: parseInt(row.pedidos),
      pct: Math.round((parseFloat(row.receita) / maxReceita) * 100),
    }));

    res.json({ kpis, grafico_mensal, status_pedidos, pedidos_recentes, estoque_baixo, top_produtos });
  } catch (error) {
    console.error('Erro ao buscar dashboard:', error);
    res.status(500).json({ error: 'Erro ao buscar dados do dashboard' });
  } finally {
    if (conn) conn.release();
  }
}

module.exports = { getDashboard };
