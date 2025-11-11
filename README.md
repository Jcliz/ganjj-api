# Ganjj API

API REST para sistema de e-commerce desenvolvida com Spring Boot.

## Requisitos

- Java 21 ou superior
- Maven 3.6+ (ou usar o wrapper incluído)

## Configuração

A aplicação possui três perfis de ambiente:

- `dev` - Desenvolvimento (padrão)
- `test` - Testes
- `prod` - Produção

Para alterar o perfil, edite o arquivo `application.properties` ou use a variável de ambiente:

```bash
spring.profiles.active=dev
```

## Comandos Úteis

### Compilar o projeto

```bash
./mvnw clean install
```

Windows:
```bash
.\mvnw.cmd clean install
```

### Iniciar o servidor

```bash
./mvnw spring-boot:run
```

Windows:
```bash
.\mvnw.cmd spring-boot:run
```

O servidor estará disponível em: `http://localhost:8080`

### Executar testes

Executar todos os testes:
```bash
./mvnw test
```

Windows:
```bash
.\mvnw.cmd test
```

### Compilar sem executar testes

```bash
./mvnw clean install -DskipTests
```

### Gerar o arquivo JAR

```bash
./mvnw package
```

O arquivo JAR será gerado em `target/ganjj-0.0.1-SNAPSHOT.jar`

### Executar o JAR

```bash
java -jar target/ganjj-0.0.1-SNAPSHOT.jar
```

### Limpar arquivos compilados

```bash
./mvnw clean
```

## Documentação da API

Após iniciar o servidor, acesse:

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

## Console H2 (Perfil dev/test)

Acesse o console do banco de dados H2 em: `http://localhost:8080/h2-console`

Configurações de conexão:
- JDBC URL: `jdbc:h2:mem:ganjjdb`
- Username: `sa`
- Password: (deixar em branco)

## Autenticação

A API usa JWT para autenticação. Para testar os endpoints protegidos:

1. Faça login no endpoint `POST /api/auth/login` com as credenciais:

Usuário Admin:
```json
{
  "email": "admin@ganjj.com",
  "password": "admin123"
}
```

Usuário comum:
```json
{
  "email": "cliente@ganjj.com",
  "password": "cliente123"
}
```

2. Copie o token retornado no campo `token`

3. No Swagger, clique em "Authorize" e cole o token

4. Agora você pode acessar os endpoints protegidos

## Estrutura do Projeto

```
src/
├── main/
│   ├── java/com/ganjj/
│   │   ├── config/          # Configurações
│   │   ├── controller/      # Controllers REST
│   │   ├── dto/             # Data Transfer Objects
│   │   ├── entities/        # Entidades JPA
│   │   ├── repository/      # Repositórios
│   │   ├── security/        # Configuração de segurança
│   │   └── service/         # Camada de serviço
│   └── resources/
│       └── application*.properties
└── test/                    # Testes
```

## Endpoints Principais

- `/api/auth/login` - Autenticação
- `/api/auth/refresh-token` - Renovar token
- `/api/users` - Gerenciamento de usuários
- `/api/products` - Produtos
- `/api/categories` - Categorias
- `/api/brands` - Marcas
- `/api/orders` - Pedidos
- `/api/shopping-bags` - Carrinho de compras
- `/api/addresses` - Endereços
- `/api/product-reviews` - Avaliações de produtos

## Tecnologias Utilizadas

- Spring Boot 3.x
- Spring Security com JWT
- Spring Data JPA
- H2 Database (dev/test)
- Maven
- Lombok
- SpringDoc OpenAPI (Swagger)

## Troubleshooting

### Porta 8080 já em uso

Se a porta 8080 estiver ocupada, você pode:

1. Matar o processo que está usando a porta:

Windows:
```bash
netstat -ano | findstr :8080
taskkill /F /PID <PID>
```

Linux/Mac:
```bash
lsof -i :8080
kill -9 <PID>
```

2. Ou alterar a porta no `application.properties`:
```properties
server.port=8081
```
