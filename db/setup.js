const db = require('./db');

async function runSetup() {
  const conn = await db.connect();
  try {
    await conn.query(`
      CREATE TABLE IF NOT EXISTS cesta (
        id         SERIAL PRIMARY KEY,
        usuario_id INTEGER NOT NULL REFERENCES usuario(id) ON DELETE CASCADE,
        criado_em  TIMESTAMP DEFAULT NOW(),
        UNIQUE(usuario_id)
      )
    `);
    await conn.query(`
      CREATE TABLE IF NOT EXISTS cesta_itens (
        id         SERIAL PRIMARY KEY,
        cesta_id   INTEGER NOT NULL REFERENCES cesta(id) ON DELETE CASCADE,
        produto_id INTEGER NOT NULL REFERENCES produto(id) ON DELETE CASCADE,
        quantidade INTEGER NOT NULL DEFAULT 1,
        added_at   TIMESTAMP DEFAULT NOW(),
        UNIQUE(cesta_id, produto_id)
      )
    `);
    await conn.query(`
      CREATE TABLE IF NOT EXISTS sale (
        id         SERIAL PRIMARY KEY,
        produto_id INTEGER NOT NULL REFERENCES produto(id) ON DELETE CASCADE,
        desconto_pct INTEGER NOT NULL CHECK (desconto_pct > 0 AND desconto_pct <= 100),
        categoria  VARCHAR(50) NOT NULL CHECK (categoria IN ('Superiores', 'Inferiores', 'Inverno')),
        ativo      BOOLEAN NOT NULL DEFAULT TRUE,
        criado_em  TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
        UNIQUE(produto_id)
      )
    `);
    console.log('DB setup: tabelas verificadas (cesta + sale).');
  } finally {
    conn.release();
  }
}

module.exports = { runSetup };
