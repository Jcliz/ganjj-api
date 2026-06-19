# GANJJ API

Backend do e-commerce **GANJJ** — loja de roupas com arquitetura de microsserviços. Cada serviço roda de forma independente e toda a comunicação com o frontend passa pelo **API Gateway**.

---

## Arquitetura

```
ganjj-api/
├── services/
│   ├── api-gateway        → porta 3000  (roteador único para o frontend)
│   ├── auth-service       → porta 3001  (autenticação + clientes)
│   ├── product-service    → porta 3002  (produtos, sale, upload de imagens)
│   ├── cart-service       → porta 3003  (carrinho de compras)
│   ├── order-service      → porta 3004  (pedidos)
│   └── dashboard-service  → porta 3005  (métricas admin)
└── db/
    ├── schema.sql         (cria todas as tabelas)
    └── seed.sql           (dados de exemplo)
```

> O frontend acessa **apenas** `http://localhost:3000`. O API Gateway encaminha cada rota para o serviço correto.

---

## Tecnologias

- **Node.js** com Express
- **PostgreSQL** como banco de dados
- **JWT** (access token 15min + refresh token 7d via cookie)
- **Cloudinary** para upload e CDN de imagens de produtos
- **Jest** para testes automatizados

---

## Pré-requisitos

- [Node.js](https://nodejs.org/) v18 ou superior
- [PostgreSQL](https://www.postgresql.org/) v14 ou superior

---

## Instalação

### 1. Instale as dependências de todos os serviços

Na raiz do projeto, execute:

```bash
npm install
```

Isso instala as dependências do pacote raiz (inclui `concurrently`) e também as de cada serviço individualmente.

> Se preferir instalar manualmente, entre em cada pasta dentro de `services/` e execute `npm install`.

### 2. Configure o banco de dados

```bash
# Crie o banco de dados
createdb ganjj_api

# Execute o schema (cria as tabelas)
psql -d ganjj_api -f db/schema.sql

# Execute o seed com dados de exemplo (opcional, mas recomendado)
psql -d ganjj_api -f db/seed.sql
```

### 3. Configure as variáveis de ambiente

Cada serviço possui seu próprio arquivo `.env`. Os arquivos já estão criados com valores padrão para desenvolvimento local — ajuste apenas a `CONNECTION_STRING` se necessário.

| Serviço | Arquivo | Variáveis principais |
|---|---|---|
| api-gateway | `services/api-gateway/.env` | `PORT`, `APP_URL` |
| auth-service | `services/auth-service/.env` | `PORT`, `CONNECTION_STRING`, `JWT_SECRET` |
| product-service | `services/product-service/.env` | `PORT`, `CONNECTION_STRING`, `CLOUDINARY_*` |
| cart-service | `services/cart-service/.env` | `PORT`, `CONNECTION_STRING`, `PRODUCT_SERVICE_URL` |
| order-service | `services/order-service/.env` | `PORT`, `CONNECTION_STRING`, `PRODUCT_SERVICE_URL` |
| dashboard-service | `services/dashboard-service/.env` | `PORT`, `CONNECTION_STRING` |

**Exemplo de `.env` para auth-service e demais serviços:**

```env
PORT=3001
CONNECTION_STRING=postgresql://postgres:SENHA@localhost:5432/ganjj_api

JWT_SECRET=sua_chave_secreta_aqui
JWT_REFRESH_SECRET=outra_chave_secreta_aqui
JWT_EXPIRES_IN=15m
JWT_REFRESH_EXPIRES_IN=7d

NODE_ENV=development
```

**Para o product-service, adicione também:**

```env
CLOUDINARY_CLOUD_NAME=seu_cloud_name
CLOUDINARY_API_KEY=sua_api_key
CLOUDINARY_API_SECRET=seu_api_secret
```

---

## Executando todos os serviços

```bash
# Inicia todos os serviços simultaneamente (recomendado)
npm start
```

Isso executa em paralelo: `auth-service`, `product-service`, `cart-service`, `order-service`, `dashboard-service` e `api-gateway`.

Para iniciar individualmente, entre na pasta do serviço e execute `node index.js`.

---

## Documentação da API (Scalar)

A documentação interativa da API está publicada online em:

**https://jcliz.github.io/ganjj-api/**

Com os serviços rodando localmente, ela também fica disponível em:

**`http://localhost:3000/api/docs`**

O JSON da especificação OpenAPI também está disponível em:

**`http://localhost:3000/api/docs.json`**

---

## Endpoints

Todos os endpoints são acessados via **API Gateway** em `http://localhost:3000`.

### Autenticação — `/api/auth`

| Método | Rota | Descrição | Auth |
|---|---|---|---|
| `POST` | `/api/auth/register` | Cria conta de usuário | Não |
| `POST` | `/api/auth/login` | Login — define cookie JWT | Não |
| `POST` | `/api/auth/logout` | Logout — remove cookie JWT | Não |
| `POST` | `/api/auth/refresh` | Renova access token | Não (cookie) |
| `GET` | `/api/auth/me` | Retorna dados do usuário autenticado | Sim |

**Payload de login:**
```json
{ "email": "usuario@email.com", "senha": "suasenha" }
```

---

### Clientes — `/api/clientes`

> Requer autenticação + papel **Admin**

| Método | Rota | Descrição |
|---|---|---|
| `GET` | `/api/clientes` | Lista todos os clientes |
| `GET` | `/api/clientes/:id` | Busca cliente por ID |
| `POST` | `/api/clientes` | Cria cliente |
| `PUT` | `/api/clientes/:id` | Atualiza cliente |
| `DELETE` | `/api/clientes/:id` | Remove cliente |

---

### Produtos — `/api/produtos`

| Método | Rota | Descrição | Auth |
|---|---|---|---|
| `GET` | `/api/produtos` | Lista todos os produtos | Não |
| `GET` | `/api/produtos/:id` | Busca produto por ID | Não |
| `POST` | `/api/produtos/upload` | Faz upload de imagem (Cloudinary) | Admin |
| `POST` | `/api/produtos` | Cria produto | Admin |
| `PUT` | `/api/produtos/:id` | Atualiza produto | Admin |
| `DELETE` | `/api/produtos/:id` | Remove produto | Admin |

---

### Sale (Promoções) — `/api/sale`

| Método | Rota | Descrição | Auth |
|---|---|---|---|
| `GET` | `/api/sale` | Lista produtos em promoção (filtro: `?categoria=`) | Não |

---

### Cesta (Carrinho) — `/api/cesta`

> Requer autenticação

| Método | Rota | Descrição |
|---|---|---|
| `GET` | `/api/cesta` | Retorna cesta do usuário autenticado |
| `POST` | `/api/cesta/itens` | Adiciona item à cesta |
| `PUT` | `/api/cesta/itens/:produto_id` | Atualiza quantidade de um item |
| `DELETE` | `/api/cesta/itens/:produto_id` | Remove item da cesta |
| `DELETE` | `/api/cesta` | Limpa toda a cesta |

---

### Pedidos — `/api/pedidos`

| Método | Rota | Descrição | Auth |
|---|---|---|---|
| `POST` | `/api/pedidos` | Cria pedido a partir da cesta | Sim |
| `GET` | `/api/pedidos/meus` | Lista pedidos do usuário autenticado | Sim |
| `GET` | `/api/pedidos/:id` | Detalhes de um pedido | Sim |
| `GET` | `/api/pedidos/admin/todos` | Lista todos os pedidos | Admin |
| `PUT` | `/api/pedidos/:id/passo` | Avança etapa do pedido | Admin |

---

### Dashboard — `/api/dashboard`

> Requer autenticação + papel **Admin**

| Método | Rota | Descrição |
|---|---|---|
| `GET` | `/api/dashboard` | Retorna métricas gerais do sistema |

---

## Testes

```bash
# Executa todos os testes
npm test

# Executa com relatório de cobertura
npm run test:coverage
```

Os arquivos de teste ficam em `services/<serviço>/tests/` e em `services/shared/tests/`.

---

## Banco de dados — Tabelas

| Tabela | Descrição |
|---|---|
| `usuario` | Usuários cadastrados (clientes e admins) |
| `produto` | Catálogo de produtos |
| `produto_tamanhos` | Tamanhos disponíveis por produto |
| `tipo_roupa` | Categorias de roupa |
| `carrinho` | Carrinhos dos usuários |
| `carrinho_itens` | Itens dentro de cada carrinho |
| `compra` | Pedidos finalizados |
| `compra_itens` | Itens de cada pedido |
| `sale` | Promoções ativas por produto |
| `loja` | Lojas físicas |
| `contato_cliente` | Mensagens de contato enviadas pelos usuários |

---

## Gitflow

Estratégia de branches utilizada no repositório do backend:

![Gitflow do backend](docs/images/gitflow.png)

---

## Sprints

Acompanhamento das sprints do projeto (boards do Kanban).

### Sprint 1

![Sprint 1 - 1](docs/images/sprint1-1.png)
![Sprint 1 - 2](docs/images/sprint1-2.png)
![Sprint 1 - final](docs/images/sprint1-final.png)

### Sprint 2

![Sprint 2 - 1](docs/images/sprint2-1.png)
![Sprint 2 - final](docs/images/sprint2-final.png)

### Sprint 3

![Sprint 3 - 1](docs/images/sprint3-1.png)
![Sprint 3 - 2](docs/images/sprint3-2.png)
![Sprint 3 - 3](docs/images/sprint3-3.png)

### Sprint 4

![Sprint 4](docs/images/sprint4.png)

### Sprint 5

![Sprint 5](docs/images/sprint5.png)
