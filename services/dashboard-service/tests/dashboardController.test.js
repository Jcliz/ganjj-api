jest.mock('../../shared/db');

const db = require('../../shared/db');
const { getDashboard } = require('../controllers/dashboardController');

function mockRes() {
  const res = {};
  res.status = jest.fn().mockReturnValue(res);
  res.json = jest.fn().mockReturnValue(res);
  return res;
}

let mockConn;

beforeEach(() => {
  mockConn = { query: jest.fn(), release: jest.fn() };
  db.connect.mockResolvedValue(mockConn);
});

describe('getDashboard', () => {
  test('retorna kpis, gráfico mensal, status, recentes, estoque baixo e top produtos', async () => {
    const req = {};
    const res = mockRes();
    const criado_em = new Date('2026-06-01T12:00:00Z');

    mockConn.query
      // KPIs
      .mockResolvedValueOnce({
        rows: [{
          receita_total: '1500.00', total_pedidos: '10', usuarios_ativos: '5',
          total_produtos: '20', sem_estoque: '2', ticket_medio: '150.00',
        }],
      })
      // Gráfico mensal (12 meses)
      .mockResolvedValueOnce({
        rows: [
          { mes_num: 5, receita: '500.00', pedidos: '3' },
          { mes_num: 6, receita: '1000.00', pedidos: '7' },
        ],
      })
      // Status dos pedidos
      .mockResolvedValueOnce({
        rows: [
          { status: 'pending', quantidade: '6' },
          { status: 'completed', quantidade: '4' },
        ],
      })
      // Pedidos recentes
      .mockResolvedValueOnce({
        rows: [{ id: 3, cliente: 'João', total: '200.00', status: 'pending', criado_em, itens: '2' }],
      })
      // Estoque baixo
      .mockResolvedValueOnce({
        rows: [{ nome: 'Camiseta', estoque: '4', feminino: false }],
      })
      // Top produtos
      .mockResolvedValueOnce({
        rows: [
          { nome: 'Calça', receita: '800.00', pedidos: '5' },
          { nome: 'Jaqueta', receita: '400.00', pedidos: '2' },
        ],
      });

    await getDashboard(req, res);

    expect(res.json).toHaveBeenCalledWith(
      expect.objectContaining({
        kpis: {
          receita_total: 1500,
          total_pedidos: 10,
          usuarios_ativos: 5,
          total_produtos: 20,
          sem_estoque: 2,
          ticket_medio: 150,
        },
        grafico_mensal: [
          { mes: 'Mai', receita: 500, pedidos: 3 },
          { mes: 'Jun', receita: 1000, pedidos: 7 },
        ],
        status_pedidos: expect.arrayContaining([
          expect.objectContaining({ label: 'Em processamento', value: 60 }),
          expect.objectContaining({ label: 'Concluído', value: 40 }),
        ]),
        pedidos_recentes: [
          expect.objectContaining({ id: '#GNJ-00003', cliente: 'João', itens: 2, total: 200, status: 'Em processamento' }),
        ],
        estoque_baixo: [{ nome: 'Camiseta', estoque: 4, categoria: 'Masc.' }],
        top_produtos: [
          expect.objectContaining({ nome: 'Calça', receita: 800, pct: 100 }),
          expect.objectContaining({ nome: 'Jaqueta', receita: 400, pct: 50 }),
        ],
      })
    );
    expect(mockConn.release).toHaveBeenCalled();
  });

  test('agrupa status desconhecido com cor neutra', async () => {
    const req = {};
    const res = mockRes();

    mockConn.query
      .mockResolvedValueOnce({
        rows: [{ receita_total: '0', total_pedidos: '0', usuarios_ativos: '0', total_produtos: '0', sem_estoque: '0', ticket_medio: '0' }],
      })
      .mockResolvedValueOnce({ rows: [] })
      .mockResolvedValueOnce({ rows: [{ status: 'desconhecido', quantidade: '1' }] })
      .mockResolvedValueOnce({ rows: [] })
      .mockResolvedValueOnce({ rows: [] })
      .mockResolvedValueOnce({ rows: [] });

    await getDashboard(req, res);

    expect(res.json).toHaveBeenCalledWith(
      expect.objectContaining({
        status_pedidos: [expect.objectContaining({ label: 'desconhecido', color: '#737373' })],
        top_produtos: [],
      })
    );
  });

  test('retorna 500 em caso de erro no banco', async () => {
    const req = {};
    const res = mockRes();
    mockConn.query.mockRejectedValueOnce(new Error('DB error'));

    await getDashboard(req, res);

    expect(res.status).toHaveBeenCalledWith(500);
    expect(res.json).toHaveBeenCalledWith({ error: 'Erro ao buscar dados do dashboard' });
    expect(mockConn.release).toHaveBeenCalled();
  });
});
