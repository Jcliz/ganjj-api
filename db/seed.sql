-- ============================================================
-- GANJJ — Seed completo
-- Ordem: tipo_roupa → usuario → produto → produto_tamanhos
--        → sale → carrinho → compra → contato → loja
-- ============================================================

-- Limpar dados (descomente se quiser resetar tudo)
-- TRUNCATE TABLE compra_itens, carrinho_itens, compra, carrinho,
--               produto_tamanhos, sale,
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
INSERT INTO produto (nome, descricao, preco, estoque, cor, status, imagem_url, popular, feminino, novo, social, tipo_roupa_id) VALUES

  -- imagem12 · Jaqueta Bomber Preta
  ('Jaqueta Bomber Preta',
   'Bomber em nylon técnico com zíper frontal e bolso no braço. Acabamento em ribana nas mangas e barra.',
   389.90, 45, 'Black', TRUE, 'https://res.cloudinary.com/dx0dzasyr/image/upload/v1780972225/imagem12_gkb2dn.png', TRUE, FALSE, FALSE, FALSE,
   (SELECT id FROM tipo_roupa WHERE nome = 'Jaquetas')),

  -- imagem13 · Jeans Slim Dark Indigo
  ('Jeans Slim Dark Indigo',
   'Denim slim em algodão selvedge japonês, lavagem escura com toque encorpado. Corte que molda sem apertar.',
   259.90, 70, 'Navy', TRUE, 'https://res.cloudinary.com/dx0dzasyr/image/upload/v1780972226/imagem13_qtpdb5.png', TRUE, FALSE, FALSE, FALSE,
   (SELECT id FROM tipo_roupa WHERE nome = 'Jeans')),

  -- imagem14 · Camisa Xadrez Flannel Amarela
  ('Camisa Xadrez Flannel Amarela',
   'Overshirt em flannel pesado com xadrez amarelo e azul. Dois bolsos frontais com aba, botões metálicos.',
   219.90, 55, 'Orange', TRUE, 'https://res.cloudinary.com/dx0dzasyr/image/upload/v1780972226/imagem14_kpwsfa.png', FALSE, FALSE, TRUE, FALSE,
   (SELECT id FROM tipo_roupa WHERE nome = 'Camisas')),

  -- imagem15 · Suéter Crewneck Azul Royal
  ('Suéter Crewneck Azul Royal',
   'Suéter em lã lambswool, gola careca clássica. Tom azul royal vibrante, ideal para looks casuais e sociais.',
   299.90, 40, 'Blue', TRUE, 'https://res.cloudinary.com/dx0dzasyr/image/upload/v1780972226/imagem15_xcyrxf.png', TRUE, FALSE, FALSE, TRUE,
   (SELECT id FROM tipo_roupa WHERE nome = 'Casacos')),

  -- imagem16 · Calça Chino Preta
  ('Calça Chino Preta',
   'Chino em sarja de algodão com leve elastano. Corte reto relaxado, passantes de cinto e bolsos laterais.',
   189.90, 80, 'Black', TRUE, 'https://res.cloudinary.com/dx0dzasyr/image/upload/v1780972226/imagem16_zdc9dj.png', FALSE, FALSE, TRUE, FALSE,
   (SELECT id FROM tipo_roupa WHERE nome = 'Calças')),

  -- imagem17 · Overshirt Estruturado Bege
  ('Overshirt Estruturado Bege',
   'Camisa-jaqueta em sarja mole com dois bolsos duplos frontais. Corte relaxado que funciona como sobreposta ou aberta.',
   239.90, 50, 'Tan', TRUE, 'https://res.cloudinary.com/dx0dzasyr/image/upload/v1780972226/imagem17_fd5zt0.png', FALSE, FALSE, TRUE, FALSE,
   (SELECT id FROM tipo_roupa WHERE nome = 'Camisas')),

  -- imagem48 · Cardigan de Malha Preto
  ('Cardigan de Malha Preto',
   'Cardigan em lã merino com botões de chifre e dois bolsos frontais. Caimento relaxado, ideal para sobreposições.',
   349.90, 35, 'Black', TRUE, 'https://res.cloudinary.com/dx0dzasyr/image/upload/v1780972226/imagem48_qt2dff.png', TRUE, FALSE, FALSE, FALSE,
   (SELECT id FROM tipo_roupa WHERE nome = 'Casacos')),

  -- imagem49 · Camiseta Manga Longa Gola Alta
  ('Camiseta Manga Longa Gola Alta',
   'Mock-neck em algodão pima de alta gramatura. Corte slim, ideal como base ou peça principal.',
   119.90, 100, 'Black', TRUE, 'https://res.cloudinary.com/dx0dzasyr/image/upload/v1780972227/imagem49_pgcxr7.png', FALSE, FALSE, TRUE, FALSE,
   (SELECT id FROM tipo_roupa WHERE nome = 'Camisetas')),

  -- imagem50 · Calça Slim Mescla Cinza
  ('Calça Slim Mescla Cinza',
   'Calça slim em flanela mescla cinza escuro. Toque macio, cintura ajustada com elástico interno.',
   209.90, 60, 'Grey', TRUE, 'https://res.cloudinary.com/dx0dzasyr/image/upload/v1780972227/imagem50_zg7dog.png', FALSE, FALSE, TRUE, FALSE,
   (SELECT id FROM tipo_roupa WHERE nome = 'Calças')),

  -- imagem51 · Calça Social Slim Preta
  ('Calça Social Slim Preta',
   'Calça social em tecido técnico stretch com queda impecável. Corte slim elegante, versátil para ocasiões formais.',
   229.90, 55, 'Black', TRUE, 'https://res.cloudinary.com/dx0dzasyr/image/upload/v1780972228/imagem51_v7nenj.png', TRUE, FALSE, FALSE, TRUE,
   (SELECT id FROM tipo_roupa WHERE nome = 'Calças')),

  -- imagem52 · Jaqueta Puffer com Capuz Verde
  ('Jaqueta Puffer com Capuz Verde',
   'Puffer de plumas sintéticas em nylon ripstop verde oliva. Capuz embutido, bolso frontal cargo e punhos em ribana.',
   479.90, 30, 'Green', TRUE, 'https://res.cloudinary.com/dx0dzasyr/image/upload/v1780972228/imagem52_y87zmw.png', TRUE, FALSE, TRUE, FALSE,
   (SELECT id FROM tipo_roupa WHERE nome = 'Jaquetas')),

  -- imagem53 · Camisa Xadrez Flannel Verde
  ('Camisa Xadrez Flannel Verde',
   'Overshirt em flannel grosso com xadrez verde e preto. Dois bolsos com abas, botões de pressão metálicos.',
   219.90, 50, 'Green', TRUE, 'https://res.cloudinary.com/dx0dzasyr/image/upload/v1780972229/imagem53_imgbqf.png', FALSE, FALSE, FALSE, FALSE,
   (SELECT id FROM tipo_roupa WHERE nome = 'Camisas')),

  -- imagem54 · Suéter Crewneck Cinza
  ('Suéter Crewneck Cinza',
   'Suéter em lã merino extrafina, gola careca. Mescla cinza clássica, toque sedoso e caimento elegante.',
   279.90, 45, 'Grey', TRUE, 'https://res.cloudinary.com/dx0dzasyr/image/upload/v1780972229/imagem54_aucoes.png', TRUE, FALSE, FALSE, TRUE,
   (SELECT id FROM tipo_roupa WHERE nome = 'Casacos')),

  -- imagem55 · Jeans Slim Azul Stone
  ('Jeans Slim Azul Stone',
   'Denim slim em algodão stretch com lavagem stone wash azul médio. Silhueta definida com conforto total.',
   239.90, 65, 'Blue', TRUE, 'https://res.cloudinary.com/dx0dzasyr/image/upload/v1780972225/imagem55_ggnofy.png', FALSE, FALSE, TRUE, FALSE,
   (SELECT id FROM tipo_roupa WHERE nome = 'Jeans')),

  -- imagem56 · Cardigan de Malha Cinza
  ('Cardigan de Malha Cinza',
   'Cardigan oversized em lã texturizada cinza mescla. Botões de madeira, dois bolsos frontais e caimento relaxado.',
   329.90, 40, 'Grey', TRUE, 'https://res.cloudinary.com/dx0dzasyr/image/upload/v1780972225/imagem56_srt7lt.png', FALSE, FALSE, FALSE, FALSE,
   (SELECT id FROM tipo_roupa WHERE nome = 'Casacos')),

  -- imagem68 · Parka Verde Oliva
  ('Parka Verde Oliva',
   'Parka longa em algodão encerado verde oliva com forro estampado removível. Capuz ajustável e múltiplos bolsos.',
   549.90, 25, 'Green', TRUE, 'https://res.cloudinary.com/dx0dzasyr/image/upload/v1780972225/imagem68_jsezgj.png', TRUE, FALSE, FALSE, FALSE,
   (SELECT id FROM tipo_roupa WHERE nome = 'Jaquetas')),

  -- imagem69 · Overshirt Jaqueta Navy
  ('Overshirt Jaqueta Navy',
   'Jaqueta-camisa em sarja rígida azul marinho com dois bolsos frontais. Corte reto oversized para layering.',
   289.90, 40, 'Navy', TRUE, 'https://res.cloudinary.com/dx0dzasyr/image/upload/v1780972225/imagem69_xt4isv.png', FALSE, FALSE, TRUE, FALSE,
   (SELECT id FROM tipo_roupa WHERE nome = 'Jaquetas')),

  -- imagem70 · Jaqueta Fleece Full-Zip Cinza
  ('Jaqueta Fleece Full-Zip Cinza',
   'Fleece sherpa de alta gramatura com zíper YKK e bolso externo com zíper. Quente, leve e de fácil layering.',
   319.90, 50, 'Grey', TRUE, 'https://res.cloudinary.com/dx0dzasyr/image/upload/v1780972225/imagem70_zwprb7.png', FALSE, FALSE, TRUE, FALSE,
   (SELECT id FROM tipo_roupa WHERE nome = 'Casacos'));

-- ─── Tamanhos por produto ────────────────────────────────────
DO $$
DECLARE
  p_bomber        INT; p_jeans_indigo   INT; p_flannel_am    INT; p_sweater_azul   INT;
  p_chino_pto     INT; p_overshirt_bg   INT; p_cardigan_pto  INT; p_ml_gola_alta   INT;
  p_slim_cinza    INT; p_social_pto     INT; p_puffer_verde  INT; p_flannel_vd     INT;
  p_sweater_cinza INT; p_jeans_stone    INT; p_cardigan_cinza INT; p_parka_verde   INT;
  p_overshirt_nv  INT; p_fleece_cinza   INT;
BEGIN
  SELECT id INTO p_bomber         FROM produto WHERE nome = 'Jaqueta Bomber Preta';
  SELECT id INTO p_jeans_indigo   FROM produto WHERE nome = 'Jeans Slim Dark Indigo';
  SELECT id INTO p_flannel_am     FROM produto WHERE nome = 'Camisa Xadrez Flannel Amarela';
  SELECT id INTO p_sweater_azul   FROM produto WHERE nome = 'Suéter Crewneck Azul Royal';
  SELECT id INTO p_chino_pto      FROM produto WHERE nome = 'Calça Chino Preta';
  SELECT id INTO p_overshirt_bg   FROM produto WHERE nome = 'Overshirt Estruturado Bege';
  SELECT id INTO p_cardigan_pto   FROM produto WHERE nome = 'Cardigan de Malha Preto';
  SELECT id INTO p_ml_gola_alta   FROM produto WHERE nome = 'Camiseta Manga Longa Gola Alta';
  SELECT id INTO p_slim_cinza     FROM produto WHERE nome = 'Calça Slim Mescla Cinza';
  SELECT id INTO p_social_pto     FROM produto WHERE nome = 'Calça Social Slim Preta';
  SELECT id INTO p_puffer_verde   FROM produto WHERE nome = 'Jaqueta Puffer com Capuz Verde';
  SELECT id INTO p_flannel_vd     FROM produto WHERE nome = 'Camisa Xadrez Flannel Verde';
  SELECT id INTO p_sweater_cinza  FROM produto WHERE nome = 'Suéter Crewneck Cinza';
  SELECT id INTO p_jeans_stone    FROM produto WHERE nome = 'Jeans Slim Azul Stone';
  SELECT id INTO p_cardigan_cinza FROM produto WHERE nome = 'Cardigan de Malha Cinza';
  SELECT id INTO p_parka_verde    FROM produto WHERE nome = 'Parka Verde Oliva';
  SELECT id INTO p_overshirt_nv   FROM produto WHERE nome = 'Overshirt Jaqueta Navy';
  SELECT id INTO p_fleece_cinza   FROM produto WHERE nome = 'Jaqueta Fleece Full-Zip Cinza';

  -- Jaquetas
  INSERT INTO produto_tamanhos VALUES (p_bomber,'P'),(p_bomber,'M'),(p_bomber,'G'),(p_bomber,'GG') ON CONFLICT DO NOTHING;
  INSERT INTO produto_tamanhos VALUES (p_puffer_verde,'P'),(p_puffer_verde,'M'),(p_puffer_verde,'G'),(p_puffer_verde,'GG') ON CONFLICT DO NOTHING;
  INSERT INTO produto_tamanhos VALUES (p_parka_verde,'P'),(p_parka_verde,'M'),(p_parka_verde,'G'),(p_parka_verde,'GG') ON CONFLICT DO NOTHING;
  INSERT INTO produto_tamanhos VALUES (p_overshirt_nv,'P'),(p_overshirt_nv,'M'),(p_overshirt_nv,'G'),(p_overshirt_nv,'GG') ON CONFLICT DO NOTHING;

  -- Camisas
  INSERT INTO produto_tamanhos VALUES (p_flannel_am,'P'),(p_flannel_am,'M'),(p_flannel_am,'G'),(p_flannel_am,'GG') ON CONFLICT DO NOTHING;
  INSERT INTO produto_tamanhos VALUES (p_overshirt_bg,'P'),(p_overshirt_bg,'M'),(p_overshirt_bg,'G'),(p_overshirt_bg,'GG') ON CONFLICT DO NOTHING;
  INSERT INTO produto_tamanhos VALUES (p_flannel_vd,'P'),(p_flannel_vd,'M'),(p_flannel_vd,'G'),(p_flannel_vd,'GG') ON CONFLICT DO NOTHING;

  -- Camisetas
  INSERT INTO produto_tamanhos VALUES (p_ml_gola_alta,'PP'),(p_ml_gola_alta,'P'),(p_ml_gola_alta,'M'),(p_ml_gola_alta,'G'),(p_ml_gola_alta,'GG') ON CONFLICT DO NOTHING;

  -- Casacos
  INSERT INTO produto_tamanhos VALUES (p_sweater_azul,'P'),(p_sweater_azul,'M'),(p_sweater_azul,'G'),(p_sweater_azul,'GG') ON CONFLICT DO NOTHING;
  INSERT INTO produto_tamanhos VALUES (p_cardigan_pto,'P'),(p_cardigan_pto,'M'),(p_cardigan_pto,'G'),(p_cardigan_pto,'GG') ON CONFLICT DO NOTHING;
  INSERT INTO produto_tamanhos VALUES (p_sweater_cinza,'P'),(p_sweater_cinza,'M'),(p_sweater_cinza,'G'),(p_sweater_cinza,'GG') ON CONFLICT DO NOTHING;
  INSERT INTO produto_tamanhos VALUES (p_cardigan_cinza,'P'),(p_cardigan_cinza,'M'),(p_cardigan_cinza,'G'),(p_cardigan_cinza,'GG') ON CONFLICT DO NOTHING;
  INSERT INTO produto_tamanhos VALUES (p_fleece_cinza,'P'),(p_fleece_cinza,'M'),(p_fleece_cinza,'G'),(p_fleece_cinza,'GG') ON CONFLICT DO NOTHING;

  -- Calças (cintura)
  INSERT INTO produto_tamanhos VALUES (p_chino_pto,'38'),(p_chino_pto,'40'),(p_chino_pto,'42'),(p_chino_pto,'44') ON CONFLICT DO NOTHING;
  INSERT INTO produto_tamanhos VALUES (p_slim_cinza,'38'),(p_slim_cinza,'40'),(p_slim_cinza,'42'),(p_slim_cinza,'44') ON CONFLICT DO NOTHING;
  INSERT INTO produto_tamanhos VALUES (p_social_pto,'38'),(p_social_pto,'40'),(p_social_pto,'42'),(p_social_pto,'44'),(p_social_pto,'46') ON CONFLICT DO NOTHING;

  -- Jeans (cintura)
  INSERT INTO produto_tamanhos VALUES (p_jeans_indigo,'38'),(p_jeans_indigo,'40'),(p_jeans_indigo,'42'),(p_jeans_indigo,'44'),(p_jeans_indigo,'46') ON CONFLICT DO NOTHING;
  INSERT INTO produto_tamanhos VALUES (p_jeans_stone,'38'),(p_jeans_stone,'40'),(p_jeans_stone,'42'),(p_jeans_stone,'44') ON CONFLICT DO NOTHING;
END $$;

-- ─── Sale ────────────────────────────────────────────────────
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
  ('Jaqueta Bomber Preta',          30, 'Inverno'),
  ('Jeans Slim Dark Indigo',        25, 'Inferiores'),
  ('Suéter Crewneck Azul Royal',    30, 'Inverno'),
  ('Calça Chino Preta',             20, 'Inferiores'),
  ('Jaqueta Puffer com Capuz Verde',35, 'Inverno'),
  ('Cardigan de Malha Preto',       25, 'Inverno'),
  ('Parka Verde Oliva',             40, 'Inverno')
) AS v(nome, desconto_pct, categoria)
JOIN produto p ON p.nome = v.nome
ON CONFLICT (produto_id) DO NOTHING;

-- ─── Carrinhos ───────────────────────────────────────────────
INSERT INTO carrinho (usuario_id) VALUES (2),(3),(4)
ON CONFLICT DO NOTHING;

INSERT INTO carrinho_itens (carrinho_id, produto_id, quantidade, tamanho)
SELECT c.id, p.id, v.qty, v.tam
FROM (VALUES
  (2, 'Jaqueta Bomber Preta',          1, 'M'),
  (2, 'Jeans Slim Dark Indigo',         1, '40'),
  (3, 'Suéter Crewneck Azul Royal',    1, 'G'),
  (3, 'Calça Social Slim Preta',        1, '40'),
  (4, 'Cardigan de Malha Cinza',        1, 'M'),
  (4, 'Camisa Xadrez Flannel Amarela',  1, 'G')
) AS v(usuario_id, nome_produto, qty, tam)
JOIN carrinho c ON c.usuario_id = v.usuario_id
JOIN produto  p ON p.nome = v.nome_produto
ON CONFLICT DO NOTHING;

-- ─── Compras ─────────────────────────────────────────────────
INSERT INTO compra (usuario_id, total, status, passo_atual, endereco_entrega, numero_rastreio) VALUES
  (2, 649.80, 'completed', 5, 'Rua das Flores, 123 - São Paulo, SP 01310-100',       'BR123456789SP'),
  (3, 579.80, 'completed', 5, 'Av. Atlântica, 500 - Rio de Janeiro, RJ 22070-000',   'BR987654321RJ'),
  (2, 479.90, 'pending',   1, 'Rua das Flores, 123 - São Paulo, SP 01310-100',        NULL),
  (4, 769.70, 'completed', 5, 'Av. do Contorno, 800 - Belo Horizonte, MG 30110-090', 'BR456123789MG'),
  (5, 609.80, 'shipping',  4, 'Rua XV de Novembro, 200 - Curitiba, PR 80020-310',    'BR741852963PR'),
  (6, 868.80, 'completed', 5, 'Av. Boa Viagem, 1000 - Recife, PE 51030-000',         'BR321654987PE');

INSERT INTO compra_itens (compra_id, produto_id, quantidade, preco, tamanho)
SELECT ci.compra_id, p.id, ci.qty, p.preco, ci.tam
FROM (VALUES
  (1, 'Jaqueta Bomber Preta',           1, 'M'),
  (1, 'Jeans Slim Dark Indigo',          1, '40'),
  (2, 'Suéter Crewneck Azul Royal',     1, 'G'),
  (2, 'Calça Chino Preta',               1, '40'),
  (3, 'Jaqueta Puffer com Capuz Verde',  1, 'G'),
  (4, 'Cardigan de Malha Preto',         1, 'M'),
  (4, 'Jeans Slim Azul Stone',           1, '42'),
  (4, 'Overshirt Estruturado Bege',      1, 'G'),
  (5, 'Overshirt Jaqueta Navy',          1, 'M'),
  (5, 'Suéter Crewneck Cinza',           1, 'G'),
  (6, 'Parka Verde Oliva',               1, 'G'),
  (6, 'Calça Social Slim Preta',         1, '40'),
  (6, 'Jaqueta Fleece Full-Zip Cinza',   1, 'M')
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
  ('GANJJ São Paulo',      'Av. Paulista, 1000 - São Paulo, SP',              '(11) 3000-0000', 'Seg-Sex: 10–20h | Sáb: 10–18h | Dom: 12–18h'),
  ('GANJJ Rio de Janeiro', 'Rua Visconde de Pirajá, 500 - Rio de Janeiro, RJ','(21) 3000-0000', 'Seg-Sex: 10–20h | Sáb: 10–18h | Dom: 12–18h'),
  ('GANJJ Belo Horizonte', 'Av. Getúlio Vargas, 1500 - Belo Horizonte, MG',  '(31) 3000-0000', 'Seg-Sex: 10–20h | Sáb: 10–18h'),
  ('GANJJ Brasília',       'Asa Sul, Av. W3 Sul, 500 - Brasília, DF',         '(61) 3000-0000', 'Seg-Sex: 10–20h | Sáb: 10–18h');

-- ─── Resultado ───────────────────────────────────────────────
SELECT
  (SELECT COUNT(*) FROM tipo_roupa)       AS tipos_roupa,
  (SELECT COUNT(*) FROM produto)          AS produtos,
  (SELECT COUNT(*) FROM produto_tamanhos) AS tamanhos_cadastrados,
  (SELECT COUNT(*) FROM sale)             AS produtos_em_sale,
  (SELECT COUNT(*) FROM usuario)          AS usuarios,
  (SELECT COUNT(*) FROM compra)           AS compras;
