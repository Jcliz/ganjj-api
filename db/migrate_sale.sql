-- Migração: criação e população da tabela sale
-- Execute no banco existente com:
--   psql -U <usuario> -d <banco> -f db/migrate_sale.sql

CREATE TABLE IF NOT EXISTS sale (
    id           SERIAL PRIMARY KEY,
    produto_id   INTEGER NOT NULL REFERENCES produto(id) ON DELETE CASCADE,
    desconto_pct INTEGER NOT NULL CHECK (desconto_pct > 0 AND desconto_pct <= 100),
    categoria    VARCHAR(50) NOT NULL CHECK (categoria IN ('Superiores', 'Inferiores', 'Inverno')),
    ativo        BOOLEAN NOT NULL DEFAULT TRUE,
    criado_em    TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(produto_id)
);

-- Popular com os produtos existentes do seed
INSERT INTO sale (produto_id, desconto_pct, categoria, ativo) VALUES
(1,  30, 'Superiores', TRUE),
(2,  25, 'Superiores', TRUE),
(4,  35, 'Inferiores', TRUE),
(5,  40, 'Superiores', TRUE),
(6,  40, 'Inverno',    TRUE),
(7,  30, 'Inferiores', TRUE),
(8,  25, 'Superiores', TRUE),
(10, 30, 'Superiores', TRUE)
ON CONFLICT (produto_id) DO NOTHING;

SELECT 'Tabela sale criada e populada com sucesso!' AS mensagem;
