const openApiSpec = {
  openapi: '3.0.3',
  info: {
    title: 'GANJJ API',
    description: `
API do e-commerce **GANJJ** — plataforma de moda com suporte a autenticação JWT, gestão de produtos, carrinho de compras, pedidos e dashboard analítico.

## Autenticação

A API utiliza **JWT via cookie HttpOnly**. Após o login ou cadastro, os tokens são definidos automaticamente nos cookies \`accessToken\` (15 min) e \`refreshToken\` (7 dias). Para ambientes não-browser, passe o token no header \`Authorization: Bearer <token>\`.

## Fluxo típico
1. \`POST /api/auth/register\` ou \`POST /api/auth/login\`
2. Usar os endpoints protegidos (cookie é enviado automaticamente)
3. Quando o \`accessToken\` expirar, chamar \`POST /api/auth/refresh\`
4. \`POST /api/auth/logout\` para encerrar a sessão
    `,
    version: '1.0.0',
    contact: {
      name: 'GANJJ Dev Team',
    },
  },
  servers: [
    {
      url: 'http://localhost:3000',
      description: 'Servidor local de desenvolvimento',
    },
  ],
  tags: [
    { name: 'Auth', description: 'Autenticação e gerenciamento de sessão' },
    { name: 'Clientes', description: 'Gerenciamento de usuários/clientes' },
    { name: 'Produtos', description: 'Catálogo de produtos' },
    { name: 'Pedidos', description: 'Criação e consulta de pedidos' },
    { name: 'Cesta', description: 'Carrinho de compras do usuário autenticado' },
    { name: 'Dashboard', description: 'Métricas e analytics do negócio' },
  ],
  components: {
    securitySchemes: {
      cookieAuth: {
        type: 'apiKey',
        in: 'cookie',
        name: 'accessToken',
        description: 'Token JWT definido automaticamente após login/register',
      },
      bearerAuth: {
        type: 'http',
        scheme: 'bearer',
        bearerFormat: 'JWT',
        description: 'Token JWT no header Authorization (alternativa ao cookie)',
      },
    },
    schemas: {
      Usuario: {
        type: 'object',
        properties: {
          id: { type: 'integer', example: 1 },
          nome: { type: 'string', example: 'João Silva' },
          email: { type: 'string', format: 'email', example: 'joao@email.com' },
          is_admin: { type: 'boolean', example: false },
          criado_em: { type: 'string', format: 'date-time', example: '2024-03-15T10:30:00.000Z' },
        },
      },
      UsuarioListItem: {
        type: 'object',
        properties: {
          id: { type: 'integer', example: 7 },
          firstName: { type: 'string', example: 'Maria' },
          lastName: { type: 'string', example: 'Oliveira' },
          email: { type: 'string', format: 'email', example: 'maria@email.com' },
          role: { type: 'boolean', description: 'true = Admin', example: false },
          status: { type: 'boolean', description: 'true = Ativo', example: true },
          joined: { type: 'string', format: 'date-time', example: '2024-01-20T08:00:00.000Z' },
        },
      },
      Produto: {
        type: 'object',
        properties: {
          id: { type: 'integer', example: 3 },
          nome: { type: 'string', example: 'Camiseta Oversized Preta' },
          descricao: { type: 'string', example: 'Camiseta oversized em algodão 100%, corte moderno.' },
          preco: { type: 'number', format: 'float', example: 89.90 },
          estoque: { type: 'integer', example: 42 },
          cor: { type: 'string', example: 'Preto' },
          status: { type: 'boolean', example: true },
          imagem_url: { type: 'string', example: 'https://exemplo.com/img/camiseta-preta.jpg' },
          popular: { type: 'boolean', example: true },
          feminino: { type: 'boolean', example: false },
          criado_em: { type: 'string', format: 'date-time', example: '2024-02-10T14:00:00.000Z' },
        },
      },
      ItemCesta: {
        type: 'object',
        properties: {
          id: { type: 'integer', example: 12 },
          produto_id: { type: 'integer', example: 3 },
          nome: { type: 'string', example: 'Camiseta Oversized Preta' },
          preco: { type: 'number', example: 89.90 },
          cor: { type: 'string', example: 'Preto' },
          imagem_url: { type: 'string', example: 'https://exemplo.com/img/camiseta-preta.jpg' },
          quantidade: { type: 'integer', example: 2 },
        },
      },
      ItemPedido: {
        type: 'object',
        required: ['produto_id', 'quantidade', 'preco'],
        properties: {
          produto_id: { type: 'integer', example: 3 },
          quantidade: { type: 'integer', example: 2 },
          preco: { type: 'number', format: 'float', example: 89.90 },
        },
      },
      Pedido: {
        type: 'object',
        properties: {
          id: { type: 'integer', example: 123 },
          codigo: { type: 'string', example: '#GNJ-00123' },
          status: {
            type: 'string',
            enum: ['pending', 'shipped', 'delivered', 'cancelled'],
            example: 'pending',
          },
          total: { type: 'number', format: 'float', example: 179.80 },
          criado_em: { type: 'string', format: 'date-time', example: '2024-05-01T16:45:00.000Z' },
        },
      },
      PedidoDetalhado: {
        type: 'object',
        properties: {
          id: { type: 'integer', example: 123 },
          codigo: { type: 'string', example: '#GNJ-00123' },
          cliente: { type: 'string', nullable: true, example: 'João Silva' },
          email: { type: 'string', nullable: true, example: 'joao@email.com' },
          status: { type: 'string', example: 'pending' },
          total: { type: 'number', example: 179.80 },
          criado_em: { type: 'string', format: 'date-time', example: '2024-05-01T16:45:00.000Z' },
          itens: {
            type: 'array',
            items: {
              type: 'object',
              properties: {
                nome: { type: 'string', example: 'Camiseta Oversized Preta' },
                quantidade: { type: 'integer', example: 2 },
                preco: { type: 'number', example: 89.90 },
                imagem_url: { type: 'string', nullable: true, example: 'https://exemplo.com/img/camiseta-preta.jpg' },
              },
            },
          },
        },
      },
      ErroGenerico: {
        type: 'object',
        properties: {
          message: { type: 'string', example: 'Mensagem de erro descritiva.' },
        },
      },
    },
  },
  paths: {
    '/api/auth/register': {
      post: {
        tags: ['Auth'],
        summary: 'Cadastrar novo usuário',
        description: 'Cria uma nova conta de usuário e retorna os dados junto com os cookies de autenticação (`accessToken` e `refreshToken`).',
        requestBody: {
          required: true,
          content: {
            'application/json': {
              schema: {
                type: 'object',
                required: ['firstName', 'lastName', 'email', 'password'],
                properties: {
                  firstName: { type: 'string', example: 'João' },
                  lastName: { type: 'string', example: 'Silva' },
                  email: { type: 'string', format: 'email', example: 'joao@email.com' },
                  password: { type: 'string', format: 'password', minLength: 6, example: 'senhaSegura123' },
                },
              },
            },
          },
        },
        responses: {
          201: {
            description: 'Usuário criado com sucesso',
            headers: {
              'Set-Cookie': {
                description: 'Cookies `accessToken` (15min) e `refreshToken` (7 dias) definidos automaticamente',
                schema: { type: 'string' },
              },
            },
            content: {
              'application/json': {
                schema: {
                  type: 'object',
                  properties: {
                    message: { type: 'string', example: 'Cadastro realizado com sucesso' },
                    usuario: { $ref: '#/components/schemas/Usuario' },
                  },
                },
              },
            },
          },
          400: {
            description: 'Campos obrigatórios ausentes',
            content: {
              'application/json': {
                schema: { $ref: '#/components/schemas/ErroGenerico' },
                example: { message: 'Todos os campos são obrigatórios.' },
              },
            },
          },
          409: {
            description: 'E-mail já cadastrado',
            content: {
              'application/json': {
                schema: { $ref: '#/components/schemas/ErroGenerico' },
                example: { message: 'E-mail já cadastrado.' },
              },
            },
          },
          500: { description: 'Erro interno do servidor' },
        },
      },
    },

    '/api/auth/login': {
      post: {
        tags: ['Auth'],
        summary: 'Login',
        description: 'Autentica o usuário com e-mail e senha. Define cookies `accessToken` e `refreshToken` na resposta.',
        requestBody: {
          required: true,
          content: {
            'application/json': {
              schema: {
                type: 'object',
                required: ['email'],
                properties: {
                  email: { type: 'string', format: 'email', example: 'joao@email.com' },
                  senha: { type: 'string', format: 'password', example: 'senhaSegura123', description: 'Campo aceito como alternativa a `password`' },
                  password: { type: 'string', format: 'password', example: 'senhaSegura123', description: 'Campo aceito como alternativa a `senha`' },
                },
              },
            },
          },
        },
        responses: {
          200: {
            description: 'Login realizado com sucesso',
            headers: {
              'Set-Cookie': {
                description: 'Cookies de autenticação definidos',
                schema: { type: 'string' },
              },
            },
            content: {
              'application/json': {
                schema: {
                  type: 'object',
                  properties: {
                    message: { type: 'string', example: 'Login realizado com sucesso' },
                    usuario: {
                      type: 'object',
                      properties: {
                        id: { type: 'integer', example: 1 },
                        nome: { type: 'string', example: 'João Silva' },
                        email: { type: 'string', example: 'joao@email.com' },
                        is_admin: { type: 'boolean', example: false },
                      },
                    },
                  },
                },
              },
            },
          },
          400: { description: 'E-mail ou senha não informados' },
          401: {
            description: 'Credenciais inválidas',
            content: {
              'application/json': {
                schema: { $ref: '#/components/schemas/ErroGenerico' },
                example: { message: 'E-mail ou senha inválidos.' },
              },
            },
          },
          500: { description: 'Erro interno do servidor' },
        },
      },
    },

    '/api/auth/logout': {
      post: {
        tags: ['Auth'],
        summary: 'Logout',
        description: 'Encerra a sessão do usuário limpando os cookies `accessToken` e `refreshToken`.',
        responses: {
          200: {
            description: 'Logout realizado com sucesso',
            content: {
              'application/json': {
                schema: {
                  type: 'object',
                  properties: {
                    message: { type: 'string', example: 'Logout realizado com sucesso' },
                  },
                },
              },
            },
          },
        },
      },
    },

    '/api/auth/refresh': {
      post: {
        tags: ['Auth'],
        summary: 'Renovar access token',
        description: 'Gera um novo `accessToken` a partir do `refreshToken` armazenado no cookie. O `refreshToken` é válido por 7 dias.',
        responses: {
          200: {
            description: 'Token renovado com sucesso',
            headers: {
              'Set-Cookie': {
                description: 'Novo `accessToken` (15min) definido no cookie',
                schema: { type: 'string' },
              },
            },
            content: {
              'application/json': {
                schema: {
                  type: 'object',
                  properties: {
                    message: { type: 'string', example: 'Token renovado com sucesso' },
                  },
                },
              },
            },
          },
          401: {
            description: 'Refresh token ausente, inválido ou expirado',
            content: {
              'application/json': {
                schema: { $ref: '#/components/schemas/ErroGenerico' },
                example: { message: 'Refresh token inválido ou expirado.' },
              },
            },
          },
        },
      },
    },

    '/api/auth/me': {
      get: {
        tags: ['Auth'],
        summary: 'Dados do usuário autenticado',
        description: 'Retorna as informações do usuário a partir do token JWT. Requer autenticação via cookie ou header.',
        security: [{ cookieAuth: [] }, { bearerAuth: [] }],
        responses: {
          200: {
            description: 'Dados do usuário autenticado',
            content: {
              'application/json': {
                schema: {
                  type: 'object',
                  properties: {
                    usuario: { $ref: '#/components/schemas/Usuario' },
                  },
                },
              },
            },
          },
          401: { description: 'Não autenticado' },
          404: { description: 'Usuário não encontrado' },
        },
      },
    },

    '/api/clientes': {
      get: {
        tags: ['Clientes'],
        summary: 'Listar clientes',
        description: 'Retorna todos os clientes/usuários cadastrados, ordenados do mais recente para o mais antigo.',
        responses: {
          200: {
            description: 'Lista de clientes',
            content: {
              'application/json': {
                schema: {
                  type: 'array',
                  items: { $ref: '#/components/schemas/UsuarioListItem' },
                },
                example: [
                  {
                    id: 7,
                    firstName: 'Maria',
                    lastName: 'Oliveira',
                    email: 'maria@email.com',
                    role: false,
                    status: true,
                    joined: '2024-01-20T08:00:00.000Z',
                  },
                  {
                    id: 1,
                    firstName: 'Admin',
                    lastName: 'GANJJ',
                    email: 'admin@ganjj.com',
                    role: true,
                    status: true,
                    joined: '2023-11-01T00:00:00.000Z',
                  },
                ],
              },
            },
          },
          500: { description: 'Erro interno do servidor' },
        },
      },
      post: {
        tags: ['Clientes'],
        summary: 'Criar cliente',
        description: 'Cria um novo cliente/usuário. Uma senha temporária é gerada automaticamente. O campo `role: "Admin"` concede permissões administrativas.',
        requestBody: {
          required: true,
          content: {
            'application/json': {
              schema: {
                type: 'object',
                required: ['firstName', 'lastName', 'email'],
                properties: {
                  firstName: { type: 'string', example: 'Carlos' },
                  lastName: { type: 'string', example: 'Mendes' },
                  email: { type: 'string', format: 'email', example: 'carlos@email.com' },
                  role: { type: 'string', description: 'Use "Admin" para conceder acesso administrativo', example: 'Admin' },
                  status: { type: 'string', description: 'Use "Active" ou "Inactive"', example: 'Active' },
                },
              },
            },
          },
        },
        responses: {
          201: {
            description: 'Cliente criado',
            content: {
              'application/json': {
                schema: { $ref: '#/components/schemas/UsuarioListItem' },
                example: {
                  id: 8,
                  firstName: 'Carlos',
                  lastName: 'Mendes',
                  email: 'carlos@email.com',
                  role: true,
                  status: true,
                  joined: '2024-05-30T12:00:00.000Z',
                },
              },
            },
          },
          400: { description: 'Campos ausentes ou e-mail inválido' },
          409: { description: 'E-mail já cadastrado' },
          500: { description: 'Erro interno do servidor' },
        },
      },
    },

    '/api/clientes/{id}': {
      get: {
        tags: ['Clientes'],
        summary: 'Buscar cliente por ID',
        parameters: [
          { name: 'id', in: 'path', required: true, schema: { type: 'integer' }, example: 7 },
        ],
        responses: {
          200: {
            description: 'Cliente encontrado',
            content: {
              'application/json': {
                schema: { $ref: '#/components/schemas/UsuarioListItem' },
              },
            },
          },
          404: {
            description: 'Cliente não encontrado',
            content: {
              'application/json': {
                schema: { $ref: '#/components/schemas/ErroGenerico' },
                example: { message: 'Usuário não encontrado.' },
              },
            },
          },
        },
      },
      put: {
        tags: ['Clientes'],
        summary: 'Atualizar cliente',
        description: 'Atualiza os dados de um cliente. Todos os campos são opcionais — envie apenas os que deseja alterar.',
        parameters: [
          { name: 'id', in: 'path', required: true, schema: { type: 'integer' }, example: 7 },
        ],
        requestBody: {
          required: true,
          content: {
            'application/json': {
              schema: {
                type: 'object',
                properties: {
                  firstName: { type: 'string', example: 'Maria' },
                  lastName: { type: 'string', example: 'Santos' },
                  email: { type: 'string', format: 'email', example: 'maria.santos@email.com' },
                  role: { type: 'string', description: '"Admin" ou qualquer outro valor', example: 'Admin' },
                  status: { type: 'string', description: '"Active" ou "Inactive"', example: 'Inactive' },
                },
              },
            },
          },
        },
        responses: {
          200: {
            description: 'Cliente atualizado',
            content: {
              'application/json': {
                schema: { $ref: '#/components/schemas/UsuarioListItem' },
              },
            },
          },
          400: { description: 'Nenhum campo enviado, e-mail inválido ou status inválido' },
          404: { description: 'Cliente não encontrado' },
          409: { description: 'E-mail já em uso por outro usuário' },
          500: { description: 'Erro interno do servidor' },
        },
      },
      delete: {
        tags: ['Clientes'],
        summary: 'Deletar cliente',
        parameters: [
          { name: 'id', in: 'path', required: true, schema: { type: 'integer' }, example: 7 },
        ],
        responses: {
          200: {
            description: 'Cliente deletado',
            content: {
              'application/json': {
                schema: {
                  type: 'object',
                  properties: {
                    message: { type: 'string', example: 'Usuário deletado com sucesso' },
                  },
                },
              },
            },
          },
          404: { description: 'Cliente não encontrado' },
          500: { description: 'Erro interno do servidor' },
        },
      },
    },

    '/api/produtos': {
      get: {
        tags: ['Produtos'],
        summary: 'Listar produtos',
        description: 'Retorna todos os produtos cadastrados, ordenados do mais recente para o mais antigo.',
        responses: {
          200: {
            description: 'Lista de produtos',
            content: {
              'application/json': {
                schema: {
                  type: 'array',
                  items: { $ref: '#/components/schemas/Produto' },
                },
                example: [
                  {
                    id: 3,
                    nome: 'Camiseta Oversized Preta',
                    descricao: 'Camiseta oversized em algodão 100%, corte moderno.',
                    preco: 89.90,
                    estoque: 42,
                    cor: 'Preto',
                    status: true,
                    imagem_url: 'https://exemplo.com/img/camiseta-preta.jpg',
                    popular: true,
                    feminino: false,
                    criado_em: '2024-02-10T14:00:00.000Z',
                  },
                ],
              },
            },
          },
          500: { description: 'Erro interno do servidor' },
        },
      },
      post: {
        tags: ['Produtos'],
        summary: 'Criar produto',
        description: 'Cadastra um novo produto no catálogo. `preco` deve ser maior que zero e `estoque` deve ser um inteiro não-negativo.',
        requestBody: {
          required: true,
          content: {
            'application/json': {
              schema: {
                type: 'object',
                required: ['nome', 'preco', 'estoque'],
                properties: {
                  nome: { type: 'string', example: 'Calça Cargo Bege' },
                  descricao: { type: 'string', example: 'Calça cargo em tecido resistente, estilo streetwear.' },
                  preco: { type: 'number', format: 'float', minimum: 0.01, example: 149.90 },
                  estoque: { type: 'integer', minimum: 0, example: 30 },
                  cor: { type: 'string', example: 'Bege' },
                  status: { type: 'boolean', default: true, example: true },
                  imagem_url: { type: 'string', example: 'https://exemplo.com/img/calca-cargo-bege.jpg' },
                  popular: { type: 'boolean', default: false, example: false },
                  feminino: { type: 'boolean', default: false, example: false },
                },
              },
            },
          },
        },
        responses: {
          201: {
            description: 'Produto criado',
            content: {
              'application/json': {
                schema: { $ref: '#/components/schemas/Produto' },
                example: {
                  id: 10,
                  nome: 'Calça Cargo Bege',
                  descricao: 'Calça cargo em tecido resistente, estilo streetwear.',
                  preco: 149.90,
                  estoque: 30,
                  cor: 'Bege',
                  status: true,
                  imagem_url: 'https://exemplo.com/img/calca-cargo-bege.jpg',
                  popular: false,
                  feminino: false,
                  criado_em: '2024-05-30T12:00:00.000Z',
                },
              },
            },
          },
          400: { description: 'Campos obrigatórios ausentes ou valores inválidos (preço/estoque)' },
          500: { description: 'Erro interno do servidor' },
        },
      },
    },

    '/api/produtos/{id}': {
      get: {
        tags: ['Produtos'],
        summary: 'Buscar produto por ID',
        parameters: [
          { name: 'id', in: 'path', required: true, schema: { type: 'integer' }, example: 3 },
        ],
        responses: {
          200: {
            description: 'Produto encontrado',
            content: {
              'application/json': {
                schema: { $ref: '#/components/schemas/Produto' },
              },
            },
          },
          404: {
            description: 'Produto não encontrado',
            content: {
              'application/json': {
                schema: { $ref: '#/components/schemas/ErroGenerico' },
                example: { message: 'Produto não encontrado.' },
              },
            },
          },
        },
      },
      put: {
        tags: ['Produtos'],
        summary: 'Atualizar produto',
        description: 'Atualiza os dados de um produto. Todos os campos são opcionais.',
        parameters: [
          { name: 'id', in: 'path', required: true, schema: { type: 'integer' }, example: 3 },
        ],
        requestBody: {
          required: true,
          content: {
            'application/json': {
              schema: {
                type: 'object',
                properties: {
                  nome: { type: 'string', example: 'Camiseta Oversized Branca' },
                  descricao: { type: 'string', example: 'Versão branca da camiseta bestseller.' },
                  preco: { type: 'number', example: 99.90 },
                  estoque: { type: 'integer', example: 15 },
                  cor: { type: 'string', example: 'Branco' },
                  status: { type: 'boolean', example: true },
                  imagem_url: { type: 'string', example: 'https://exemplo.com/img/camiseta-branca.jpg' },
                  popular: { type: 'boolean', example: false },
                  feminino: { type: 'boolean', example: true },
                },
              },
            },
          },
        },
        responses: {
          200: {
            description: 'Produto atualizado',
            content: {
              'application/json': {
                schema: { $ref: '#/components/schemas/Produto' },
              },
            },
          },
          400: { description: 'Nenhum campo enviado ou valor inválido' },
          404: { description: 'Produto não encontrado' },
          500: { description: 'Erro interno do servidor' },
        },
      },
      delete: {
        tags: ['Produtos'],
        summary: 'Deletar produto',
        parameters: [
          { name: 'id', in: 'path', required: true, schema: { type: 'integer' }, example: 3 },
        ],
        responses: {
          200: {
            description: 'Produto deletado',
            content: {
              'application/json': {
                schema: {
                  type: 'object',
                  properties: {
                    message: { type: 'string', example: 'Produto deletado com sucesso' },
                  },
                },
              },
            },
          },
          404: { description: 'Produto não encontrado' },
          500: { description: 'Erro interno do servidor' },
        },
      },
    },

    '/api/pedidos': {
      post: {
        tags: ['Pedidos'],
        summary: 'Criar pedido',
        description: `Cria um novo pedido com os itens informados. A operação é atômica (transação): se qualquer item falhar (estoque insuficiente, produto inexistente), nenhum item é processado. O estoque dos produtos é decrementado automaticamente.`,
        requestBody: {
          required: true,
          content: {
            'application/json': {
              schema: {
                type: 'object',
                required: ['itens'],
                properties: {
                  usuario_id: { type: 'integer', nullable: true, description: 'ID do usuário autenticado (opcional)', example: 1 },
                  itens: {
                    type: 'array',
                    minItems: 1,
                    items: { $ref: '#/components/schemas/ItemPedido' },
                  },
                },
              },
              example: {
                usuario_id: 1,
                itens: [
                  { produto_id: 3, quantidade: 2, preco: 89.90 },
                  { produto_id: 5, quantidade: 1, preco: 149.90 },
                ],
              },
            },
          },
        },
        responses: {
          201: {
            description: 'Pedido criado com sucesso',
            content: {
              'application/json': {
                schema: { $ref: '#/components/schemas/Pedido' },
                example: {
                  id: 123,
                  codigo: '#GNJ-00123',
                  status: 'pending',
                  total: 329.70,
                  criado_em: '2024-05-01T16:45:00.000Z',
                },
              },
            },
          },
          400: {
            description: 'Itens vazios, campos ausentes ou estoque insuficiente',
            content: {
              'application/json': {
                schema: { $ref: '#/components/schemas/ErroGenerico' },
                example: { message: 'Estoque insuficiente para o produto: Camiseta Oversized Preta (disponível: 1)' },
              },
            },
          },
          404: { description: 'Produto não encontrado' },
          500: { description: 'Erro interno do servidor' },
        },
      },
    },

    '/api/pedidos/{id}': {
      get: {
        tags: ['Pedidos'],
        summary: 'Buscar pedido por ID',
        description: 'Retorna os detalhes de um pedido incluindo todos os itens, dados do cliente e status atual.',
        parameters: [
          { name: 'id', in: 'path', required: true, schema: { type: 'integer' }, example: 123 },
        ],
        responses: {
          200: {
            description: 'Pedido encontrado',
            content: {
              'application/json': {
                schema: { $ref: '#/components/schemas/PedidoDetalhado' },
                example: {
                  id: 123,
                  codigo: '#GNJ-00123',
                  cliente: 'João Silva',
                  email: 'joao@email.com',
                  status: 'pending',
                  total: 329.70,
                  criado_em: '2024-05-01T16:45:00.000Z',
                  itens: [
                    {
                      nome: 'Camiseta Oversized Preta',
                      quantidade: 2,
                      preco: 89.90,
                      imagem_url: 'https://exemplo.com/img/camiseta-preta.jpg',
                    },
                    {
                      nome: 'Calça Cargo Bege',
                      quantidade: 1,
                      preco: 149.90,
                      imagem_url: 'https://exemplo.com/img/calca-cargo-bege.jpg',
                    },
                  ],
                },
              },
            },
          },
          404: {
            description: 'Pedido não encontrado',
            content: {
              'application/json': {
                schema: { $ref: '#/components/schemas/ErroGenerico' },
                example: { message: 'Pedido não encontrado.' },
              },
            },
          },
          500: { description: 'Erro interno do servidor' },
        },
      },
    },

    '/api/cesta': {
      get: {
        tags: ['Cesta'],
        summary: 'Ver cesta do usuário',
        description: 'Retorna todos os itens da cesta de compras do usuário autenticado. Se o usuário não tiver cesta criada, retorna lista vazia.',
        security: [{ cookieAuth: [] }, { bearerAuth: [] }],
        responses: {
          200: {
            description: 'Itens da cesta',
            content: {
              'application/json': {
                schema: {
                  type: 'object',
                  properties: {
                    itens: {
                      type: 'array',
                      items: { $ref: '#/components/schemas/ItemCesta' },
                    },
                  },
                },
                example: {
                  itens: [
                    {
                      id: 12,
                      produto_id: 3,
                      nome: 'Camiseta Oversized Preta',
                      preco: 89.90,
                      cor: 'Preto',
                      imagem_url: 'https://exemplo.com/img/camiseta-preta.jpg',
                      quantidade: 2,
                    },
                  ],
                },
              },
            },
          },
          401: { description: 'Não autenticado' },
          500: { description: 'Erro interno do servidor' },
        },
      },
      delete: {
        tags: ['Cesta'],
        summary: 'Limpar cesta',
        description: 'Remove todos os itens da cesta do usuário autenticado.',
        security: [{ cookieAuth: [] }, { bearerAuth: [] }],
        responses: {
          200: {
            description: 'Cesta limpa com sucesso',
            content: {
              'application/json': {
                schema: {
                  type: 'object',
                  properties: {
                    message: { type: 'string', example: 'Cesta limpa' },
                  },
                },
              },
            },
          },
          401: { description: 'Não autenticado' },
          500: { description: 'Erro interno do servidor' },
        },
      },
    },

    '/api/cesta/itens': {
      post: {
        tags: ['Cesta'],
        summary: 'Adicionar item à cesta',
        description: 'Adiciona um produto à cesta. Se o produto já estiver na cesta, incrementa a quantidade. A cesta é criada automaticamente se não existir. Valida disponibilidade de estoque.',
        security: [{ cookieAuth: [] }, { bearerAuth: [] }],
        requestBody: {
          required: true,
          content: {
            'application/json': {
              schema: {
                type: 'object',
                required: ['produto_id'],
                properties: {
                  produto_id: { type: 'integer', example: 3 },
                  quantidade: { type: 'integer', minimum: 1, default: 1, example: 2 },
                },
              },
            },
          },
        },
        responses: {
          201: {
            description: 'Item adicionado',
            content: {
              'application/json': {
                schema: {
                  type: 'object',
                  properties: {
                    message: { type: 'string', example: 'Item adicionado à cesta' },
                  },
                },
              },
            },
          },
          400: {
            description: 'produto_id ausente, quantidade inválida ou estoque insuficiente',
            content: {
              'application/json': {
                schema: { $ref: '#/components/schemas/ErroGenerico' },
                example: { message: 'Estoque insuficiente. Disponível: 1' },
              },
            },
          },
          401: { description: 'Não autenticado' },
          404: { description: 'Produto não encontrado ou inativo' },
          500: { description: 'Erro interno do servidor' },
        },
      },
    },

    '/api/cesta/itens/{produto_id}': {
      put: {
        tags: ['Cesta'],
        summary: 'Atualizar quantidade de item',
        description: 'Altera a quantidade de um produto específico na cesta do usuário autenticado.',
        security: [{ cookieAuth: [] }, { bearerAuth: [] }],
        parameters: [
          { name: 'produto_id', in: 'path', required: true, schema: { type: 'integer' }, example: 3 },
        ],
        requestBody: {
          required: true,
          content: {
            'application/json': {
              schema: {
                type: 'object',
                required: ['quantidade'],
                properties: {
                  quantidade: { type: 'integer', minimum: 1, example: 3 },
                },
              },
            },
          },
        },
        responses: {
          200: {
            description: 'Quantidade atualizada',
            content: {
              'application/json': {
                schema: {
                  type: 'object',
                  properties: {
                    message: { type: 'string', example: 'Quantidade atualizada' },
                  },
                },
              },
            },
          },
          400: { description: 'Quantidade inválida' },
          401: { description: 'Não autenticado' },
          404: { description: 'Cesta ou item não encontrado' },
          500: { description: 'Erro interno do servidor' },
        },
      },
      delete: {
        tags: ['Cesta'],
        summary: 'Remover item da cesta',
        description: 'Remove um produto específico da cesta do usuário autenticado.',
        security: [{ cookieAuth: [] }, { bearerAuth: [] }],
        parameters: [
          { name: 'produto_id', in: 'path', required: true, schema: { type: 'integer' }, example: 3 },
        ],
        responses: {
          200: {
            description: 'Item removido',
            content: {
              'application/json': {
                schema: {
                  type: 'object',
                  properties: {
                    message: { type: 'string', example: 'Item removido da cesta' },
                  },
                },
              },
            },
          },
          401: { description: 'Não autenticado' },
          404: { description: 'Cesta não encontrada' },
          500: { description: 'Erro interno do servidor' },
        },
      },
    },

    '/api/dashboard': {
      get: {
        tags: ['Dashboard'],
        summary: 'Métricas do negócio',
        description: `Retorna um conjunto completo de métricas e analytics para o painel administrativo, incluindo KPIs, gráfico dos últimos 12 meses, distribuição de status de pedidos, pedidos recentes, produtos com estoque baixo e top produtos por receita.`,
        responses: {
          200: {
            description: 'Dashboard com todas as métricas',
            content: {
              'application/json': {
                schema: {
                  type: 'object',
                  properties: {
                    kpis: {
                      type: 'object',
                      properties: {
                        receita_total: { type: 'number', example: 45820.50 },
                        total_pedidos: { type: 'integer', example: 312 },
                        usuarios_ativos: { type: 'integer', example: 87 },
                        total_produtos: { type: 'integer', example: 54 },
                        sem_estoque: { type: 'integer', example: 3 },
                        ticket_medio: { type: 'number', example: 146.86 },
                      },
                    },
                    grafico_mensal: {
                      type: 'array',
                      items: {
                        type: 'object',
                        properties: {
                          mes: { type: 'string', example: 'Jan' },
                          receita: { type: 'number', example: 3200.00 },
                          pedidos: { type: 'integer', example: 22 },
                        },
                      },
                    },
                    status_pedidos: {
                      type: 'array',
                      items: {
                        type: 'object',
                        properties: {
                          label: { type: 'string', example: 'Entregue' },
                          value: { type: 'number', description: 'Percentual (%)', example: 62 },
                          color: { type: 'string', example: '#22c55e' },
                        },
                      },
                    },
                    pedidos_recentes: {
                      type: 'array',
                      items: {
                        type: 'object',
                        properties: {
                          id: { type: 'string', example: '#GNJ-00123' },
                          cliente: { type: 'string', example: 'João Silva' },
                          itens: { type: 'integer', example: 3 },
                          total: { type: 'number', example: 329.70 },
                          status: { type: 'string', example: 'Em processamento' },
                          data: { type: 'string', example: '01/mai' },
                        },
                      },
                    },
                    estoque_baixo: {
                      type: 'array',
                      description: 'Produtos com estoque <= 50, ordenados pelo menor estoque',
                      items: {
                        type: 'object',
                        properties: {
                          nome: { type: 'string', example: 'Moletom Cinza P' },
                          estoque: { type: 'integer', example: 2 },
                          categoria: { type: 'string', enum: ['Fem.', 'Masc.'], example: 'Masc.' },
                        },
                      },
                    },
                    top_produtos: {
                      type: 'array',
                      description: 'Top 5 produtos por receita gerada',
                      items: {
                        type: 'object',
                        properties: {
                          nome: { type: 'string', example: 'Camiseta Oversized Preta' },
                          receita: { type: 'number', example: 8091.00 },
                          pedidos: { type: 'integer', example: 90 },
                          pct: { type: 'number', description: 'Percentual em relação ao produto líder (0–100)', example: 100 },
                        },
                      },
                    },
                  },
                },
                example: {
                  kpis: {
                    receita_total: 45820.50,
                    total_pedidos: 312,
                    usuarios_ativos: 87,
                    total_produtos: 54,
                    sem_estoque: 3,
                    ticket_medio: 146.86,
                  },
                  grafico_mensal: [
                    { mes: 'Jun', receita: 2800.00, pedidos: 19 },
                    { mes: 'Jul', receita: 3500.00, pedidos: 24 },
                    { mes: 'Mai', receita: 4100.00, pedidos: 28 },
                  ],
                  status_pedidos: [
                    { label: 'Entregue', value: 62, color: '#22c55e' },
                    { label: 'Enviado', value: 18, color: '#3b82f6' },
                    { label: 'Em processamento', value: 14, color: '#f59e0b' },
                    { label: 'Cancelado', value: 6, color: '#ef4444' },
                  ],
                  pedidos_recentes: [
                    { id: '#GNJ-00123', cliente: 'João Silva', itens: 3, total: 329.70, status: 'Em processamento', data: '01/mai' },
                  ],
                  estoque_baixo: [
                    { nome: 'Moletom Cinza P', estoque: 2, categoria: 'Masc.' },
                  ],
                  top_produtos: [
                    { nome: 'Camiseta Oversized Preta', receita: 8091.00, pedidos: 90, pct: 100 },
                  ],
                },
              },
            },
          },
          500: { description: 'Erro interno do servidor' },
        },
      },
    },
  },
};

module.exports = openApiSpec;
