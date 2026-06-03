-- ============================================================
-- GANJJ — Seed completo
-- Ordem: tipo_roupa → usuario → produto → produto_tamanhos
--        → sale → carrinho → compra → contato → loja
-- ============================================================

-- Limpar dados (descomente se quiser resetar tudo)
-- TRUNCATE TABLE compra_itens, carrinho_itens, compra, carrinho,
--               produto_tamanhos, sale, cesta_itens, cesta,
--               produto, tipo_roupa, usuario,
--               contato_cliente, loja
-- RESTART IDENTITY CASCADE;

-- ─── Tipos de roupa ──────────────────────────────────────────
INSERT INTO tipo_roupa (nome) VALUES
  ('Camisas'),
  ('Camisetas'),
  ('Casacos'),
  ('Jaquetas'),
  ('Calças'),
  ('Jeans')
ON CONFLICT (nome) DO NOTHING;

-- ─── Usuários ────────────────────────────────────────────────
-- Senha padrão: 123456 (bcrypt)
INSERT INTO usuario (nome, email, senha, is_admin, status) VALUES
  ('Admin GANJJ',    'admin@ganjj.com',              '$2b$10$zceRy4uQyfkIyjAqkSIVReYuiXtjd96lpDfaAIMT2vzQ3XglLI4sW', TRUE,  TRUE),
  ('João Silva',     'joao@example.com',             '$2b$10$zceRy4uQyfkIyjAqkSIVReYuiXtjd96lpDfaAIMT2vzQ3XglLI4sW', FALSE, TRUE),
  ('Maria Santos',   'maria@example.com',            '$2b$10$zceRy4uQyfkIyjAqkSIVReYuiXtjd96lpDfaAIMT2vzQ3XglLI4sW', FALSE, TRUE),
  ('Carlos Oliveira','carlos@example.com',           '$2b$10$zceRy4uQyfkIyjAqkSIVReYuiXtjd96lpDfaAIMT2vzQ3XglLI4sW', FALSE, TRUE),
  ('Ana Costa',      'ana@example.com',              '$2b$10$zceRy4uQyfkIyjAqkSIVReYuiXtjd96lpDfaAIMT2vzQ3XglLI4sW', FALSE, TRUE),
  ('Pedro Gomes',    'pedro@example.com',            '$2b$10$zceRy4uQyfkIyjAqkSIVReYuiXtjd96lpDfaAIMT2vzQ3XglLI4sW', FALSE, TRUE)
ON CONFLICT (email) DO NOTHING;

-- ─── Produtos ────────────────────────────────────────────────
-- Cores usam os nomes da COR_PALETTE do frontend:
--   Black, Blue, Brown, Green, Grey, Orange, Pink, Red, Tan, Sage, Navy, Cream
-- Colunas: nome, descricao, preco, estoque, cor, status, imagem_url, popular, feminino, novo, social, tipo_roupa_id
INSERT INTO produto (nome, descricao, preco, estoque, cor, status, imagem_url, popular, feminino, novo, social, tipo_roupa_id) VALUES
  -- Camisetas masculinas
  ('Camiseta Básica Preta',
   'Camiseta de algodão 100% confortável, corte regular.',
   49.90, 150, 'Black', TRUE, NULL, TRUE,  FALSE, FALSE, FALSE,
   (SELECT id FROM tipo_roupa WHERE nome = 'Camisetas')),

  ('Camiseta Básica Creme',
   'Camiseta de algodão 100% confortável, tom neutro.',
   49.90, 120, 'Cream', TRUE, NULL, TRUE,  FALSE, FALSE, FALSE,
   (SELECT id FROM tipo_roupa WHERE nome = 'Camisetas')),

  ('Camiseta Básica Azul',
   'Camiseta de algodão, cor azul vibrante.',
   49.90, 100, 'Blue',  TRUE, NULL, FALSE, FALSE, TRUE,  FALSE,
   (SELECT id FROM tipo_roupa WHERE nome = 'Camisetas')),

  -- Camisas masculinas
  ('Camisa Oxford Navy',
   'Camisa Oxford clássica em algodão certificado, corte ligeiramente boxudo.',
   119.90, 80, 'Navy',  TRUE, NULL, TRUE,  FALSE, FALSE, TRUE,
   (SELECT id FROM tipo_roupa WHERE nome = 'Camisas')),

  ('Camisa Linho Bege',
   'Camisa de linho 100%, fresca e versátil para o verão.',
   139.90, 60, 'Tan',   TRUE, NULL, FALSE, FALSE, TRUE,  TRUE,
   (SELECT id FROM tipo_roupa WHERE nome = 'Camisas')),

  -- Calças masculinas
  ('Calça Cargo Verde',
   'Calça cargo com múltiplos bolsos, conforto máximo.',
   139.90, 55, 'Green', TRUE, NULL, FALSE, FALSE, TRUE,  FALSE,
   (SELECT id FROM tipo_roupa WHERE nome = 'Calças')),

  -- Jeans masculinos
  ('Calça Jeans Slim Navy',
   'Jeans slim em denim selvedge japonês, desbotamento sutil.',
   189.90, 70, 'Navy',  TRUE, NULL, TRUE,  FALSE, FALSE, FALSE,
   (SELECT id FROM tipo_roupa WHERE nome = 'Jeans')),

  ('Calça Jeans Reta Cinza',
   'Jeans reta em denim premium com leve elastano.',
   179.90, 45, 'Grey',  TRUE, NULL, FALSE, FALSE, TRUE,  FALSE,
   (SELECT id FROM tipo_roupa WHERE nome = 'Jeans')),

  -- Casacos masculinos
  ('Casaco Tricô Marrom',
   'Casaco de tricô em lã merino, regulador de temperatura natural.',
   249.90, 40, 'Brown', TRUE, NULL, TRUE,  FALSE, FALSE, FALSE,
   (SELECT id FROM tipo_roupa WHERE nome = 'Casacos')),

  ('Casaco Moletom Cinza',
   'Moletom pesado de algodão orgânico, fleece interno.',
   199.90, 65, 'Grey',  TRUE, NULL, FALSE, FALSE, TRUE,  FALSE,
   (SELECT id FROM tipo_roupa WHERE nome = 'Casacos')),

  -- Jaquetas masculinas
  ('Jaqueta Corta-Vento Preta',
   'Jaqueta leve corta-vento, ideal para dias frios.',
   299.90, 30, 'Black', TRUE, NULL, TRUE,  FALSE, TRUE,  FALSE,
   (SELECT id FROM tipo_roupa WHERE nome = 'Jaquetas')),

  -- Camisetas femininas
  ('Camiseta Cropped Rosa',
   'Camiseta cropped em algodão pima, caimento perfeito.',
   59.90, 90, 'Pink',   TRUE, NULL, TRUE,  TRUE,  TRUE,  FALSE,
   (SELECT id FROM tipo_roupa WHERE nome = 'Camisetas')),

  ('Camiseta Oversized Sage',
   'Camiseta oversized em algodão orgânico, tom sálvia.',
   69.90, 75, 'Sage',   TRUE, NULL, FALSE, TRUE,  TRUE,  FALSE,
   (SELECT id FROM tipo_roupa WHERE nome = 'Camisetas')),

  -- Camisas femininas
  ('Camisa Social Creme',
   'Camisa social feminina em viscose premium, modelagem slim.',
   149.90, 50, 'Cream', TRUE, NULL, TRUE,  TRUE,  TRUE,  TRUE,
   (SELECT id FROM tipo_roupa WHERE nome = 'Camisas')),

  -- Calças femininas
  ('Calça Wide Leg Preta',
   'Calça wide leg em tecido fluido, cintura alta.',
   159.90, 60, 'Black', TRUE, NULL, TRUE,  TRUE,  TRUE,  TRUE,
   (SELECT id FROM tipo_roupa WHERE nome = 'Calças')),

  -- Jeans femininos
  ('Jeans Skinny Azul',
   'Jeans skinny de algodão com elastano, conforto total.',
   169.90, 80, 'Blue',  TRUE, NULL, TRUE,  TRUE,  FALSE, FALSE,
   (SELECT id FROM tipo_roupa WHERE nome = 'Jeans')),

  -- Casacos femininos
  ('Casaco Lã Rosê',
   'Casaco de lã merino tingido na peça, acabamento premium.',
   279.90, 35, 'Pink',  TRUE, NULL, TRUE,  TRUE,  TRUE,  FALSE,
   (SELECT id FROM tipo_roupa WHERE nome = 'Casacos')),

  -- Jaquetas femininas
  ('Jaqueta Jeans Azul',
   'Jaqueta jeans clássica feminina, lavagem média.',
   219.90, 40, 'Blue',  TRUE, NULL, FALSE, TRUE,  FALSE, FALSE,
   (SELECT id FROM tipo_roupa WHERE nome = 'Jaquetas'));

-- ─── Tamanhos por produto ────────────────────────────────────
-- p1=Camiseta Básica Preta, p2=Camiseta Básica Creme, p3=Camiseta Básica Azul
-- p4=Camisa Oxford Navy, p5=Camisa Linho Bege
-- p6=Calça Cargo Verde
-- p7=Calça Jeans Slim Navy, p8=Calça Jeans Reta Cinza
-- p9=Casaco Tricô Marrom, p10=Casaco Moletom Cinza
-- p11=Jaqueta Corta-Vento Preta
-- p12=Camiseta Cropped Rosa, p13=Camiseta Oversized Sage
-- p14=Camisa Social Creme
-- p15=Calça Wide Leg Preta
-- p16=Jeans Skinny Azul
-- p17=Casaco Lã Rosê
-- p18=Jaqueta Jeans Azul

-- Usamos subquery para pegar o id pelo nome, evitando hardcode de sequência
DO $$
DECLARE
  id_p1  INT; id_p2  INT; id_p3  INT; id_p4  INT; id_p5  INT;
  id_p6  INT; id_p7  INT; id_p8  INT; id_p9  INT; id_p10 INT;
  id_p11 INT; id_p12 INT; id_p13 INT; id_p14 INT; id_p15 INT;
  id_p16 INT; id_p17 INT; id_p18 INT;
BEGIN
  SELECT id INTO id_p1  FROM produto WHERE nome = 'Camiseta Básica Preta';
  SELECT id INTO id_p2  FROM produto WHERE nome = 'Camiseta Básica Creme';
  SELECT id INTO id_p3  FROM produto WHERE nome = 'Camiseta Básica Azul';
  SELECT id INTO id_p4  FROM produto WHERE nome = 'Camisa Oxford Navy';
  SELECT id INTO id_p5  FROM produto WHERE nome = 'Camisa Linho Bege';
  SELECT id INTO id_p6  FROM produto WHERE nome = 'Calça Cargo Verde';
  SELECT id INTO id_p7  FROM produto WHERE nome = 'Calça Jeans Slim Navy';
  SELECT id INTO id_p8  FROM produto WHERE nome = 'Calça Jeans Reta Cinza';
  SELECT id INTO id_p9  FROM produto WHERE nome = 'Casaco Tricô Marrom';
  SELECT id INTO id_p10 FROM produto WHERE nome = 'Casaco Moletom Cinza';
  SELECT id INTO id_p11 FROM produto WHERE nome = 'Jaqueta Corta-Vento Preta';
  SELECT id INTO id_p12 FROM produto WHERE nome = 'Camiseta Cropped Rosa';
  SELECT id INTO id_p13 FROM produto WHERE nome = 'Camiseta Oversized Sage';
  SELECT id INTO id_p14 FROM produto WHERE nome = 'Camisa Social Creme';
  SELECT id INTO id_p15 FROM produto WHERE nome = 'Calça Wide Leg Preta';
  SELECT id INTO id_p16 FROM produto WHERE nome = 'Jeans Skinny Azul';
  SELECT id INTO id_p17 FROM produto WHERE nome = 'Casaco Lã Rosê';
  SELECT id INTO id_p18 FROM produto WHERE nome = 'Jaqueta Jeans Azul';

  -- Camisetas masculinas (PP–GG)
  INSERT INTO produto_tamanhos VALUES (id_p1,'PP'),(id_p1,'P'),(id_p1,'M'),(id_p1,'G'),(id_p1,'GG') ON CONFLICT DO NOTHING;
  INSERT INTO produto_tamanhos VALUES (id_p2,'PP'),(id_p2,'P'),(id_p2,'M'),(id_p2,'G'),(id_p2,'GG') ON CONFLICT DO NOTHING;
  INSERT INTO produto_tamanhos VALUES (id_p3,'P'),(id_p3,'M'),(id_p3,'G'),(id_p3,'GG')               ON CONFLICT DO NOTHING;

  -- Camisas masculinas
  INSERT INTO produto_tamanhos VALUES (id_p4,'P'),(id_p4,'M'),(id_p4,'G'),(id_p4,'GG')               ON CONFLICT DO NOTHING;
  INSERT INTO produto_tamanhos VALUES (id_p5,'P'),(id_p5,'M'),(id_p5,'G')                             ON CONFLICT DO NOTHING;

  -- Calças masculinas (cintura)
  INSERT INTO produto_tamanhos VALUES (id_p6,'38'),(id_p6,'40'),(id_p6,'42'),(id_p6,'44')             ON CONFLICT DO NOTHING;

  -- Jeans masculinos (cintura)
  INSERT INTO produto_tamanhos VALUES (id_p7,'38'),(id_p7,'40'),(id_p7,'42'),(id_p7,'44'),(id_p7,'46') ON CONFLICT DO NOTHING;
  INSERT INTO produto_tamanhos VALUES (id_p8,'38'),(id_p8,'40'),(id_p8,'42'),(id_p8,'44')              ON CONFLICT DO NOTHING;

  -- Casacos e jaquetas masculinos
  INSERT INTO produto_tamanhos VALUES (id_p9,'P'),(id_p9,'M'),(id_p9,'G'),(id_p9,'GG')                ON CONFLICT DO NOTHING;
  INSERT INTO produto_tamanhos VALUES (id_p10,'PP'),(id_p10,'P'),(id_p10,'M'),(id_p10,'G'),(id_p10,'GG') ON CONFLICT DO NOTHING;
  INSERT INTO produto_tamanhos VALUES (id_p11,'P'),(id_p11,'M'),(id_p11,'G'),(id_p11,'GG')             ON CONFLICT DO NOTHING;

  -- Camisetas femininas
  INSERT INTO produto_tamanhos VALUES (id_p12,'PP'),(id_p12,'P'),(id_p12,'M'),(id_p12,'G')            ON CONFLICT DO NOTHING;
  INSERT INTO produto_tamanhos VALUES (id_p13,'P'),(id_p13,'M'),(id_p13,'G'),(id_p13,'GG')            ON CONFLICT DO NOTHING;

  -- Camisa feminina
  INSERT INTO produto_tamanhos VALUES (id_p14,'PP'),(id_p14,'P'),(id_p14,'M'),(id_p14,'G')            ON CONFLICT DO NOTHING;

  -- Calças femininas (roupas)
  INSERT INTO produto_tamanhos VALUES (id_p15,'PP'),(id_p15,'P'),(id_p15,'M'),(id_p15,'G'),(id_p15,'GG') ON CONFLICT DO NOTHING;

  -- Jeans femininos (cintura)
  INSERT INTO produto_tamanhos VALUES (id_p16,'36'),(id_p16,'38'),(id_p16,'40'),(id_p16,'42')          ON CONFLICT DO NOTHING;

  -- Casaco e jaqueta femininos
  INSERT INTO produto_tamanhos VALUES (id_p17,'P'),(id_p17,'M'),(id_p17,'G')                           ON CONFLICT DO NOTHING;
  INSERT INTO produto_tamanhos VALUES (id_p18,'PP'),(id_p18,'P'),(id_p18,'M'),(id_p18,'G')             ON CONFLICT DO NOTHING;
END $$;

-- ─── Sale ────────────────────────────────────────────────────
-- Tabela criada aqui para permitir rodar seed.sql de forma independente
CREATE TABLE IF NOT EXISTS sale (
    id           SERIAL PRIMARY KEY,
    produto_id   INTEGER NOT NULL REFERENCES produto(id) ON DELETE CASCADE,
    desconto_pct INTEGER NOT NULL CHECK (desconto_pct > 0 AND desconto_pct <= 100),
    categoria    VARCHAR(50) NOT NULL CHECK (categoria IN ('Superiores', 'Inferiores', 'Inverno')),
    ativo        BOOLEAN NOT NULL DEFAULT TRUE,
    criado_em    TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(produto_id)
);

INSERT INTO sale (produto_id, desconto_pct, categoria, ativo)
SELECT p.id, v.desconto_pct, v.categoria, TRUE
FROM (VALUES
  ('Camiseta Básica Preta',    30, 'Superiores'),
  ('Camiseta Básica Creme',    25, 'Superiores'),
  ('Calça Jeans Slim Navy',    35, 'Inferiores'),
  ('Camisa Oxford Navy',       20, 'Superiores'),
  ('Jaqueta Corta-Vento Preta',40, 'Inverno'),
  ('Casaco Tricô Marrom',      30, 'Inverno'),
  ('Camiseta Cropped Rosa',    25, 'Superiores'),
  ('Jeans Skinny Azul',        35, 'Inferiores'),
  ('Casaco Lã Rosê',           30, 'Inverno')
) AS v(nome, desconto_pct, categoria)
JOIN produto p ON p.nome = v.nome
ON CONFLICT (produto_id) DO NOTHING;

-- ─── Carrinhos (schema original) ────────────────────────────
INSERT INTO carrinho (usuario_id) VALUES (2),(3),(4)
ON CONFLICT DO NOTHING;

INSERT INTO carrinho_itens (carrinho_id, produto_id, quantidade, tamanho)
SELECT c.id, p.id, v.qty, v.tam
FROM (VALUES
  (2, 'Camiseta Básica Preta',   2, 'M'),
  (2, 'Calça Jeans Slim Navy',   1, '40'),
  (3, 'Camiseta Cropped Rosa',   1, 'P'),
  (3, 'Jeans Skinny Azul',       2, '38'),
  (4, 'Camiseta Básica Creme',   1, 'GG'),
  (4, 'Camisa Oxford Navy',      1, 'G')
) AS v(usuario_id, nome_produto, qty, tam)
JOIN carrinho c ON c.usuario_id = v.usuario_id
JOIN produto  p ON p.nome = v.nome_produto
ON CONFLICT DO NOTHING;

-- ─── Compras ─────────────────────────────────────────────────
INSERT INTO compra (usuario_id, total, status) VALUES
  (2, 379.80, 'completed'),
  (3, 559.70, 'completed'),
  (2, 299.90, 'pending'),
  (4, 499.70, 'completed'),
  (5, 469.80, 'shipping'),
  (6, 748.70, 'completed');

INSERT INTO compra_itens (compra_id, produto_id, quantidade, preco, tamanho)
SELECT ci.compra_id, p.id, ci.qty, p.preco, ci.tam
FROM (VALUES
  (1, 'Camiseta Básica Preta',   2, 'M'),
  (1, 'Calça Jeans Slim Navy',   1, '40'),
  (2, 'Camiseta Cropped Rosa',   1, 'P'),
  (2, 'Jeans Skinny Azul',       2, '38'),
  (3, 'Jaqueta Corta-Vento Preta',1,'G'),
  (4, 'Camiseta Básica Creme',   1, 'GG'),
  (4, 'Calça Jeans Slim Navy',   1, '42'),
  (4, 'Camisa Oxford Navy',      1, 'G'),
  (5, 'Jaqueta Jeans Azul',      1, 'P'),
  (5, 'Casaco Lã Rosê',          1, 'M'),
  (6, 'Casaco Tricô Marrom',     2, 'G'),
  (6, 'Calça Wide Leg Preta',    1, 'M')
) AS ci(compra_id, nome_produto, qty, tam)
JOIN produto p ON p.nome = ci.nome_produto;

-- ─── Contatos ────────────────────────────────────────────────
INSERT INTO contato_cliente (nome, email, mensagem) VALUES
  ('João Silva',        'joao@example.com',              'Gostaria de saber sobre a política de trocas'),
  ('Maria Santos',      'maria@example.com',             'Não recebi meu pedido ainda'),
  ('Roberto Alves',     'roberto.alves@example.com',     'Produto chegou com defeito'),
  ('Fernanda Lima',     'fernanda.lima@example.com',     'Qual é o tempo de entrega?'),
  ('Lucas Martins',     'lucas.martins@example.com',     'Gostaria de cancelar meu pedido'),
  ('Camila Souza',      'camila.souza@example.com',      'O produto corresponde à descrição?');

-- ─── Lojas ───────────────────────────────────────────────────
INSERT INTO loja (nome, endereco, telefone, horas_abertura) VALUES
  ('GANJJ São Paulo',      'Av. Paulista, 1000 - São Paulo, SP',            '(11) 3000-0000', 'Seg-Sex: 10–20h | Sáb: 10–18h | Dom: 12–18h'),
  ('GANJJ Rio de Janeiro', 'Rua Visconde de Pirajá, 500 - Rio de Janeiro, RJ','(21) 3000-0000','Seg-Sex: 10–20h | Sáb: 10–18h | Dom: 12–18h'),
  ('GANJJ Belo Horizonte', 'Av. Getúlio Vargas, 1500 - Belo Horizonte, MG', '(31) 3000-0000', 'Seg-Sex: 10–20h | Sáb: 10–18h'),
  ('GANJJ Brasília',       'Asa Sul, Av. W3 Sul, 500 - Brasília, DF',       '(61) 3000-0000', 'Seg-Sex: 10–20h | Sáb: 10–18h');

-- ─── Resultado ───────────────────────────────────────────────
SELECT
  (SELECT COUNT(*) FROM tipo_roupa)     AS tipos_roupa,
  (SELECT COUNT(*) FROM produto)        AS produtos,
  (SELECT COUNT(*) FROM produto_tamanhos) AS tamanhos_cadastrados,
  (SELECT COUNT(*) FROM sale)           AS produtos_em_sale,
  (SELECT COUNT(*) FROM usuario)        AS usuarios,
  (SELECT COUNT(*) FROM compra)         AS compras;
