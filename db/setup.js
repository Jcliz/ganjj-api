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
    console.log('DB setup: tabelas de cesta verificadas.');
  } finally {
    conn.release();
  }
}

module.exports = { runSetup };
