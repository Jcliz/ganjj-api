const db = require('./db');

async function runSetup() {
  const conn = await db.connect();
  try {
    // Tabelas do carrinho
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
        id           SERIAL PRIMARY KEY,
        produto_id   INTEGER NOT NULL REFERENCES produto(id) ON DELETE CASCADE,
        desconto_pct INTEGER NOT NULL CHECK (desconto_pct > 0 AND desconto_pct <= 100),
        categoria    VARCHAR(50) NOT NULL CHECK (categoria IN ('Superiores', 'Inferiores', 'Inverno')),
        ativo        BOOLEAN NOT NULL DEFAULT TRUE,
        criado_em    TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
        UNIQUE(produto_id)
      )
    `);

    // Tabela de tipos de roupa
    await conn.query(`
      CREATE TABLE IF NOT EXISTS tipo_roupa (
        id   SERIAL PRIMARY KEY,
        nome VARCHAR(50) UNIQUE NOT NULL
      )
    `);

    // Seed dos tipos padrão
    await conn.query(`
      INSERT INTO tipo_roupa (nome)
      VALUES ('Camisas'), ('Camisetas'), ('Casacos'), ('Jaquetas'), ('Calças'), ('Jeans')
      ON CONFLICT (nome) DO NOTHING
    `);

    // Tabela de tamanhos por produto (N:N)
    await conn.query(`
      CREATE TABLE IF NOT EXISTS produto_tamanhos (
        produto_id INTEGER NOT NULL REFERENCES produto(id) ON DELETE CASCADE,
        tamanho    VARCHAR(20) NOT NULL,
        PRIMARY KEY (produto_id, tamanho)
      )
    `);

    // Migração: adiciona colunas em produto (se não existirem)
    await conn.query(`ALTER TABLE produto ADD COLUMN IF NOT EXISTS tipo_roupa_id INTEGER REFERENCES tipo_roupa(id)`);
    await conn.query(`ALTER TABLE produto ADD COLUMN IF NOT EXISTS novo    BOOLEAN NOT NULL DEFAULT TRUE`);
    await conn.query(`ALTER TABLE produto ADD COLUMN IF NOT EXISTS social  BOOLEAN NOT NULL DEFAULT FALSE`);

    // Migração: remove colunas antigas se ainda existirem
    await conn.query(`ALTER TABLE produto DROP COLUMN IF EXISTS tipo_roupa`);
    await conn.query(`ALTER TABLE produto DROP COLUMN IF EXISTS tamanhos`);

    console.log('DB setup OK: cesta, sale, tipo_roupa, produto_tamanhos.');
  } finally {
    conn.release();
  }
}

module.exports = { runSetup };
