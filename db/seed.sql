-- Limpar dados existentes (opcional - comentar se não quiser apagar)
-- TRUNCATE TABLE compra_itens CASCADE;
-- TRUNCATE TABLE carrinho_itens CASCADE;
-- TRUNCATE TABLE compra CASCADE;
-- TRUNCATE TABLE carrinho CASCADE;
-- TRUNCATE TABLE produto CASCADE;
-- TRUNCATE TABLE usuario CASCADE;
-- TRUNCATE TABLE contato_cliente CASCADE;
-- TRUNCATE TABLE loja CASCADE;

-- Inserir usuários
INSERT INTO usuario (nome, email, senha, is_admin, status) VALUES
('Admin User', 'admin@ganjj.com', '$2b$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcg7b3XeKeUxWdeS86E36P4/TVm', TRUE, TRUE),
('João Silva', 'joao@example.com', '$2b$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcg7b3XeKeUxWdeS86E36P4/TVm', FALSE, TRUE),
('Maria Santos', 'maria@example.com', '$2b$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcg7b3XeKeUxWdeS86E36P4/TVm', FALSE, TRUE),
('Carlos Oliveira', 'carlos@example.com', '$2b$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcg7b3XeKeUxWdeS86E36P4/TVm', FALSE, TRUE),
('Ana Costa', 'ana@example.com', '$2b$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcg7b3XeKeUxWdeS86E36P4/TVm', FALSE, TRUE),
('Pedro Gomes', 'pedro@example.com', '$2b$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcg7b3XeKeUxWdeS86E36P4/TVm', FALSE, TRUE);

-- Inserir produtos
INSERT INTO produto (nome, descricao, preco, estoque, cor, status, imagem_url, popular, feminino) VALUES
('Camiseta Básica Branca', 'Camiseta de algodão 100% confortável', 49.90, 150, 'Branco', TRUE, '/images/camiseta-branca.jpg', TRUE, FALSE),
('Camiseta Básica Preta', 'Camiseta de algodão 100% confortável', 49.90, 120, 'Preto', TRUE, '/images/camiseta-preta.jpg', TRUE, FALSE),
('Camiseta Básica Azul', 'Camiseta de algodão 100% confortável', 49.90, 100, 'Azul', TRUE, '/images/camiseta-azul.jpg', FALSE, FALSE),
('Calça Jeans Premium', 'Calça jeans de alta qualidade com stretch', 129.90, 80, 'Azul Escuro', TRUE, '/images/calca-jeans.jpg', TRUE, FALSE),
('Vestido Floral', 'Vestido feminino em tecido leve com estampa floral', 159.90, 45, 'Floral', TRUE, '/images/vestido-floral.jpg', TRUE, TRUE),
('Jaqueta Jeans', 'Jaqueta jeans clássica para mulheres', 199.90, 35, 'Azul', TRUE, '/images/jaqueta-jeans.jpg', FALSE, TRUE),
('Shorts Jeans', 'Shorts jeans curto e confortável', 89.90, 120, 'Azul Médio', TRUE, '/images/shorts-jeans.jpg', FALSE, TRUE),
('Blusa Social', 'Blusa social em viscose premium', 119.90, 60, 'Branco', TRUE, '/images/blusa-social.jpg', TRUE, TRUE),
('Calça Cargo', 'Calça cargo com múltiplos bolsos', 139.90, 55, 'Cáqui', TRUE, '/images/calca-cargo.jpg', FALSE, FALSE),
('Polo Masculina', 'Camisa polo de algodão puro', 99.90, 90, 'Verde', TRUE, '/images/polo-verde.jpg', FALSE, FALSE),
('Meia Longa', 'Meia longa em algodão macio', 25.90, 300, 'Preto', TRUE, '/images/meia-longa.jpg', FALSE, TRUE);

-- Inserir carrinhos
INSERT INTO carrinho (usuario_id) VALUES
(2),
(3),
(4);

-- Inserir itens no carrinho
INSERT INTO carrinho_itens (carrinho_id, produto_id, quantidade, tamanho) VALUES
(1, 1, 2, 'M'),
(1, 4, 1, '42'),
(2, 5, 1, 'P'),
(2, 7, 2, 'G'),
(3, 2, 1, 'GG'),
(3, 10, 1, 'G');

-- Inserir compras
INSERT INTO compra (usuario_id, total, status) VALUES
(2, 349.70, 'completed'),
(3, 589.70, 'completed'),
(2, 199.90, 'pending'),
(4, 449.70, 'completed'),
(5, 299.80, 'shipping'),
(6, 599.00, 'completed'),
(2, 749.60, 'completed'),
(3, 189.90, 'completed');

-- Inserir itens das compras
INSERT INTO compra_itens (compra_id, produto_id, quantidade, preco, tamanho) VALUES
-- Compra 1
(1, 1, 2, 49.90, 'M'),
(1, 4, 1, 129.90, '42'),
-- Compra 2
(2, 5, 1, 159.90, 'P'),
(2, 7, 2, 89.90, 'G'),
-- Compra 3
(3, 10, 1, 99.90, 'G'),
(3, 8, 1, 119.90, 'M'),
-- Compra 4
(4, 2, 1, 49.90, 'GG'),
(4, 4, 1, 129.90, '40'),
(4, 8, 1, 119.90, 'M'),
-- Compra 5
(5, 6, 1, 199.90, 'P'),
(5, 10, 1, 99.90, 'G'),
-- Compra 6
(6, 1, 3, 49.90, 'PP'),
(6, 6, 2, 199.90, 'M'),
-- Compra 7
(7, 5, 1, 159.90, 'M'),
(7, 8, 2, 119.90, 'P'),
(7, 9, 1, 139.90, '38'),
-- Compra 8
(8, 3, 1, 49.90, 'G'),
(8, 7, 1, 89.90, 'M'),
(8, 11, 2, 25.90, 'Único');

-- Inserir produtos em sale
-- produto_id | desconto_pct | categoria
INSERT INTO sale (produto_id, desconto_pct, categoria, ativo) VALUES
(1,  30, 'Superiores', TRUE),  -- Camiseta Básica Branca   49.90 → 34.93
(2,  25, 'Superiores', TRUE),  -- Camiseta Básica Preta    49.90 → 37.43
(4,  35, 'Inferiores', TRUE),  -- Calça Jeans Premium     129.90 → 84.44
(5,  40, 'Superiores', TRUE),  -- Vestido Floral          159.90 → 95.94
(6,  40, 'Inverno',    TRUE),  -- Jaqueta Jeans           199.90 → 119.94
(7,  30, 'Inferiores', TRUE),  -- Shorts Jeans             89.90 → 62.93
(8,  25, 'Superiores', TRUE),  -- Blusa Social            119.90 → 89.93
(10, 30, 'Superiores', TRUE);  -- Polo Masculina           99.90 → 69.93

-- Inserir contatos de clientes
INSERT INTO contato_cliente (nome, email, mensagem) VALUES
('João Silva', 'joao@example.com', 'Gostaria de saber sobre a política de trocas'),
('Maria Santos', 'maria@example.com', 'Não recebi meu pedido ainda'),
('Roberto Alves', 'roberto.alves@example.com', 'Produto chegou com defeito'),
('Fernanda Lima', 'fernanda.lima@example.com', 'Qual é o tempo de entrega?'),
('Lucas Martins', 'lucas.martins@example.com', 'Gostaria de cancelar meu pedido'),
('Camila Souza', 'camila.souza@example.com', 'O produto corresponde à descrição?'),
('Gustavo Costa', 'gustavo.costa@example.com', 'Vocês fazem entregas no meu estado?'),
('Patricia Oliveira', 'patricia.oliveira@example.com', 'Produto excelente, muito satisfeito');

-- Inserir lojas
INSERT INTO loja (nome, endereco, telefone, horas_abertura) VALUES
('GANJJ São Paulo', 'Av. Paulista, 1000 - São Paulo, SP', '(11) 3000-0000', 'Seg-Sex: 10:00-20:00, Sab: 10:00-18:00, Dom: 12:00-18:00'),
('GANJJ Rio de Janeiro', 'Rua Visconde de Piraja, 500 - Rio de Janeiro, RJ', '(21) 3000-0000', 'Seg-Sex: 10:00-20:00, Sab: 10:00-18:00, Dom: 12:00-18:00'),
('GANJJ Belo Horizonte', 'Av. Getúlio Vargas, 1500 - Belo Horizonte, MG', '(31) 3000-0000', 'Seg-Sex: 10:00-20:00, Sab: 10:00-18:00'),
('GANJJ Salvador', 'Av. Océan, 2000 - Salvador, BA', '(71) 3000-0000', 'Seg-Sex: 10:00-20:00, Sab: 10:00-18:00, Dom: 14:00-20:00'),
('GANJJ Brasília', 'Asa Sul, Av. W3 Sul, 500 - Brasília, DF', '(61) 3000-0000', 'Seg-Sex: 10:00-20:00, Sab: 10:00-18:00');

-- Mensagem de conclusão
SELECT 'Banco de dados populado com sucesso!' as mensagem;

-- Estatísticas dos dados inseridos
SELECT 
    (SELECT COUNT(*) FROM usuario) as total_usuarios,
    (SELECT COUNT(*) FROM produto) as total_produtos,
    (SELECT COUNT(*) FROM compra) as total_compras,
    (SELECT SUM(total) FROM compra) as valor_total_vendas,
    (SELECT COUNT(*) FROM contato_cliente) as total_contatos,
    (SELECT COUNT(*) FROM loja) as total_lojas;
