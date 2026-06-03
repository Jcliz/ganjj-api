CREATE TABLE usuario (
    id SERIAL PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    senha VARCHAR(255) NOT NULL,
    is_admin BOOLEAN DEFAULT FALSE,
    status BOOLEAN DEFAULT TRUE,
    criado_em TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE tipo_roupa (
    id   SERIAL PRIMARY KEY,
    nome VARCHAR(50) UNIQUE NOT NULL
);

CREATE TABLE produto (
    id            SERIAL PRIMARY KEY,
    nome          VARCHAR(255) NOT NULL,
    descricao     TEXT,
    preco         NUMERIC(10, 2) NOT NULL,
    estoque       INTEGER NOT NULL,
    cor           VARCHAR(50),
    status        BOOLEAN DEFAULT TRUE,
    imagem_url    VARCHAR(255),
    popular       BOOLEAN DEFAULT FALSE,
    feminino      BOOLEAN DEFAULT FALSE,
    novo          BOOLEAN DEFAULT TRUE,
    social        BOOLEAN DEFAULT FALSE,
    tipo_roupa_id INTEGER REFERENCES tipo_roupa(id),
    criado_em     TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE produto_tamanhos (
    produto_id INTEGER NOT NULL REFERENCES produto(id) ON DELETE CASCADE,
    tamanho    VARCHAR(20) NOT NULL,
    PRIMARY KEY (produto_id, tamanho)
);

CREATE TABLE carrinho (
    id SERIAL PRIMARY KEY,
    usuario_id INTEGER REFERENCES usuario(id),
    criado_em TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE carrinho_itens (
    id SERIAL PRIMARY KEY,
    carrinho_id INTEGER REFERENCES carrinho(id),
    produto_id INTEGER REFERENCES produto(id),
    quantidade INTEGER NOT NULL,
    tamanho VARCHAR(20),
    UNIQUE(carrinho_id, produto_id, tamanho)
);

CREATE TABLE compra (
    id SERIAL PRIMARY KEY,
    usuario_id INTEGER REFERENCES usuario(id),
    total NUMERIC(10, 2) NOT NULL,
    status VARCHAR(50) DEFAULT 'pending',
    passo_atual INTEGER DEFAULT 0,
    endereco_entrega TEXT,
    numero_rastreio VARCHAR(100),
    criado_em TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE compra_itens (
    id SERIAL PRIMARY KEY,
    compra_id INTEGER REFERENCES compra(id),
    produto_id INTEGER REFERENCES produto(id),
    quantidade INTEGER NOT NULL,
    preco NUMERIC(10, 2) NOT NULL,
    tamanho VARCHAR(20)
);

CREATE TABLE contato_cliente (
    id SERIAL PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    mensagem TEXT NOT NULL,
    criado_em TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE loja (
    id SERIAL PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    endereco TEXT NOT NULL,
    telefone VARCHAR(50),
    horas_abertura VARCHAR(255)
);

CREATE TABLE sale (
    id SERIAL PRIMARY KEY,
    produto_id INTEGER NOT NULL REFERENCES produto(id) ON DELETE CASCADE,
    desconto_pct INTEGER NOT NULL CHECK (desconto_pct > 0 AND desconto_pct <= 100),
    categoria VARCHAR(50) NOT NULL CHECK (categoria IN ('Superiores', 'Inferiores', 'Inverno')),
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    criado_em TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(produto_id)
);
