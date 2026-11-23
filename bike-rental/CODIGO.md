# API de Aluguel de Bicicletas

API REST para gerenciar clientes, bicicletas e locações, com autenticação JWT, perfis (ADMIN/CLIENTE), auditoria automática, documentação Swagger e um frontend simples.

**Stack:** Java 21 · Spring Boot 3.3 · Spring Data JPA · Spring Security + JWT · H2 · Swagger (springdoc) · HTML/JS puro.

---

## 1. Como rodar

### Com Docker/Podman

```bash
docker compose up --build        # ou: podman compose up --build
```

| O quê         | Endereço                                  |
|---------------|-------------------------------------------|
| Frontend      | http://localhost:8080                     |
| Swagger       | http://localhost:8080/swagger-ui.html     |
| Console do H2 | http://localhost:8080/h2-console          |

O banco fica no volume `bikedata`, então os dados sobrevivem a reinicializações. Para **zerar tudo**: `docker compose down -v`.

### Sem Docker

Precisa de Java 21 e Maven:

```bash
mvn spring-boot:run
```

Nesse caso o banco é criado na pasta `./data` do projeto.

### Usuários de demonstração

Criados automaticamente na primeira execução (`DataSeeder`), junto com 3 bicicletas:

| Usuário | Senha      | Perfil  |
|---------|------------|---------|
| admin   | admin123   | ADMIN   |
| cliente | cliente123 | CLIENTE |

> Troque essas senhas (ou apague os usuários) se for publicar o projeto em algum lugar.

---

## 2. Estrutura do código

```
src/main/java/com/bikerental/
├── BikeRentalApplication.java   ponto de entrada; define timezone/locale pt-BR
├── config/
│   ├── DataSeeder.java          dados iniciais (admin, cliente demo, bicicletas)
│   ├── JpaAuditConfig.java      informa quem é o usuário logado para a auditoria
│   └── OpenApiConfig.java       configura o Swagger e o botão "Authorize"
├── security/
│   ├── SecurityConfig.java      quais rotas são públicas e quais exigem perfil
│   ├── JwtService.java          gera e valida tokens JWT
│   └── JwtFilter.java           lê o token de cada requisição e autentica
├── model/                       entidades JPA (tabelas) e enums
├── repository/                  interfaces Spring Data (acesso ao banco)
├── dto/                         objetos de entrada (Request) e saída (Response)
├── service/                     regras de negócio
├── controller/                  endpoints REST
└── exception/                   erros de negócio e tratamento global

src/main/resources/
├── application.properties       configuração (banco, timezone, JWT, Swagger)
└── static/                      frontend (index.html + app.js)
```

### Como uma requisição percorre as camadas

```
Navegador/Swagger
   │  GET /api/locacoes   (Authorization: Bearer <token>)
   ▼
JwtFilter          → lê o token, descobre usuário e perfil
SecurityConfig     → confere se o perfil pode acessar a rota
Controller         → recebe e valida o DTO de entrada (@Valid)
Service            → aplica as regras de negócio
Repository         → lê/grava no banco (JPA)
Service/DTO        → converte a entidade em DTO de resposta
Controller         → devolve JSON
```

Se qualquer camada lançar um erro, o `GlobalExceptionHandler` converte em uma resposta JSON padronizada:

```json
{ "status": 422, "mensagem": "Bicicleta não está disponível", "erros": [] }
```

### Modelo de dados

| Tabela      | Campos principais                                                                 |
|-------------|-----------------------------------------------------------------------------------|
| `usuario`   | id, username (único), password (BCrypt), role (`ADMIN`/`CLIENTE`) + auditoria     |
| `cliente`   | id, nome, cpf (único), telefone, email, usuario_id (único) + auditoria            |
| `bicicleta` | id, codigo (único), modelo, status (`DISPONIVEL`/`ALUGADA`/`MANUTENCAO`)          |
| `locacao`   | id, cliente_id, bicicleta_id, data_retirada, data_devolucao, valor, status (`ATIVA`/`FINALIZADA`) |

Relacionamentos: `cliente` → 1:1 → `usuario`; `locacao` → N:1 → `cliente` e N:1 → `bicicleta`.

### DTOs

As entidades **nunca** saem da API. Cada recurso tem um `Request` (o que o cliente envia, com validações como `@NotBlank`, `@Email`, `@Pattern`) e um `Response` (o que a API devolve). Exemplo: `UsuarioResponse` nunca inclui a senha.

### Auditoria automática

`Usuario` e `Cliente` herdam de `Auditable`, que tem 4 colunas preenchidas sozinhas pelo Spring Data:

- `data_criacao` / `criado_por`
- `data_modificacao` / `modificado_por`

O `criado_por` vem do usuário do token (`JpaAuditConfig`). Registros criados pelo seed aparecem como `sistema`.

### Autenticação JWT

1. `POST /api/auth/login` com usuário e senha → devolve um token (válido por 2 horas, configurável em `app.jwt.expiration-minutes`).
2. Todas as outras chamadas enviam o cabeçalho `Authorization: Bearer <token>`.
3. O token carrega o username e o perfil, então o servidor não precisa consultar o banco a cada requisição.
4. Token ausente/inválido/expirado → `401`. Perfil sem permissão → `403`.

A chave de assinatura vem da variável `JWT_SECRET` (mínimo 32 caracteres).

### Permissões por perfil

| Rota                          | ADMIN | CLIENTE                         |
|-------------------------------|:-----:|---------------------------------|
| `POST /api/auth/login`        | público | público                       |
| `GET /api/bicicletas`         | ✔     | ✔                               |
| `POST/PUT/DELETE /api/bicicletas` | ✔ | ✘                               |
| `/api/usuarios/**`            | ✔     | ✘                               |
| `/api/clientes/**`            | ✔     | ✘ (exceto `GET /api/clientes/me`) |
| `GET /api/locacoes`           | todas | só as próprias                  |
| `POST /api/locacoes`          | ✔ (informa `clienteId`) | ✔ (aluga para si) |
| `PATCH /api/locacoes/{id}/devolver` | ✔ | só as próprias            |
| `DELETE /api/locacoes/{id}`   | ✔     | ✘                               |

### Regras de negócio (em `service/`)

- Só se aluga bicicleta `DISPONIVEL`; ao alugar vira `ALUGADA`, ao devolver volta a `DISPONIVEL`.
- Cada cliente pode ter **uma locação ativa** por vez.
- Valor = **R$ 5,00 por hora iniciada**, mínimo 1 hora (constante `VALOR_HORA` em `LocacaoService`).
- O status `ALUGADA` não pode ser definido manualmente; bicicleta alugada não pode ser alterada de status nem excluída.
- CPF, código da bicicleta e username são únicos (conflito → `409`).
- O usuário vinculado a um cliente precisa ter perfil `CLIENTE` e não pode estar vinculado a outro cliente.
- Locação só pode ser excluída depois de finalizada.

---

## 3. Como criar um admin ou um cliente novo

Não existe tela de cadastro; isso é feito pelo **Swagger** (ou `curl`), logado como ADMIN.

### 3.1 Logar no Swagger

1. Abra http://localhost:8080/swagger-ui.html
2. Em **Autenticação → `POST /api/auth/login`**, clique em *Try it out*, use:
   ```json
   { "username": "admin", "password": "admin123" }
   ```
3. Copie o valor de `token` da resposta.
4. Clique em **Authorize** (cadeado, no topo), cole o token (sem a palavra `Bearer`) e confirme.

### 3.2 Criar um novo ADMIN

`POST /api/usuarios`:

```json
{ "username": "maria", "password": "senha123", "role": "ADMIN" }
```

Pronto: ela já consegue logar e usar tudo. (Senha com no mínimo 6 caracteres.)

### 3.3 Criar um novo CLIENTE (2 passos)

O cliente é uma pessoa (nome, CPF...) ligada a um usuário de login. Primeiro o usuário, depois o cliente.

**Passo 1** — `POST /api/usuarios`:

```json
{ "username": "joao", "password": "senha123", "role": "CLIENTE" }
```

Anote o `id` devolvido (por exemplo, `3`).

**Passo 2** — `POST /api/clientes`, usando esse id em `usuarioId`:

```json
{
  "nome": "João Silva",
  "cpf": "12345678909",
  "telefone": "(49) 98888-7777",
  "email": "joao@exemplo.com",
  "usuarioId": 3
}
```

O CPF deve ter 11 dígitos, só números. Agora `joao / senha123` consegue entrar no frontend e alugar bicicletas.

### 3.4 O mesmo com `curl`

```bash
# login
TOKEN=$(curl -s -X POST localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"admin123"}' | sed 's/.*"token":"\([^"]*\)".*/\1/')

# criar usuário CLIENTE
curl -X POST localhost:8080/api/usuarios \
  -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' \
  -d '{"username":"joao","password":"senha123","role":"CLIENTE"}'

# criar cliente (troque usuarioId pelo id devolvido acima)
curl -X POST localhost:8080/api/clientes \
  -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' \
  -d '{"nome":"João Silva","cpf":"12345678909","telefone":"(49) 98888-7777","email":"joao@exemplo.com","usuarioId":3}'
```

### 3.5 Trocar senha, excluir

- **Trocar senha:** `PUT /api/usuarios/{id}` com o mesmo `username` e `role` e a nova `password`. Se `password` vier vazia ou ausente, a senha atual é mantida.
- **Excluir usuário:** `DELETE /api/usuarios/{id}` (dá `409` se houver um cliente vinculado; exclua o cliente antes).
- **Excluir cliente:** `DELETE /api/clientes/{id}` (dá `409` se ele tiver locações).

---

## 4. Como ver o banco de dados

### 4.1 Console web do H2 (mais fácil)

1. Abra http://localhost:8080/h2-console
2. Preencha:

   | Campo        | Valor                          |
      |--------------|--------------------------------|
   | Driver Class | `org.h2.Driver`                |
   | JDBC URL     | `jdbc:h2:file:./data/bikerental` |
   | User Name    | `sa`                           |
   | Password     | *(vazio)*                      |

3. Clique em **Connect**. As tabelas `USUARIO`, `CLIENTE`, `BICICLETA` e `LOCACAO` aparecem à esquerda.

Consultas úteis:

```sql
SELECT * FROM usuario;
SELECT * FROM cliente;
SELECT * FROM bicicleta;

-- locações com nome do cliente e código da bicicleta
SELECT l.id, c.nome, b.codigo, l.data_retirada, l.data_devolucao, l.valor, l.status
FROM locacao l
JOIN cliente c ON c.id = l.cliente_id
JOIN bicicleta b ON b.id = l.bicicleta_id
ORDER BY l.id DESC;

-- auditoria: quem criou/alterou cada usuário
SELECT username, role, data_criacao, criado_por, data_modificacao, modificado_por FROM usuario;
```

> As senhas aparecem como hash BCrypt (`$2a$10$...`), nunca em texto puro.

> O console do H2 é só para desenvolvimento. Em produção, remova `spring.h2.console.enabled=true` do `application.properties` (e a rota `/h2-console/**` do `SecurityConfig`).

### 4.2 Usando um cliente SQL externo (DBeaver etc.)

O banco é de arquivo e fica aberto pela aplicação, então conexões externas por arquivo conflitam. Prefira o console web acima.

### 4.3 Onde ficam os arquivos do banco

- **Docker/Podman:** no volume `bikedata` (`podman volume inspect bikedata` mostra o caminho).
- **Sem Docker:** na pasta `./data/` (arquivo `bikerental.mv.db`).

Para começar do zero: `docker compose down -v` (container) ou apague a pasta `data/` (local). O `DataSeeder` recria admin, cliente demo e bicicletas.

---

## 5. Frontend

Em `src/main/resources/static/` (`index.html` + `app.js`), servido pelo próprio Spring Boot. Sem framework, sem build.

- **Login:** guarda o token no `sessionStorage` e o envia em toda chamada à API. Se a API responde `401`, volta para a tela de login.
- **Bicicletas:** todos veem; ADMIN adiciona, envia para manutenção/libera e exclui.
- **Clientes:** aba visível apenas para ADMIN (somente consulta).
- **Locações:** formulário para alugar (ADMIN escolhe o cliente), tabela com histórico e botão **Devolver**, que mostra o valor cobrado.

---

## 6. Configurações (`application.properties`)

| Propriedade                         | Para que serve                                       |
|-------------------------------------|------------------------------------------------------|
| `spring.datasource.url`             | caminho do banco H2                                  |
| `spring.jpa.hibernate.ddl-auto=update` | cria/atualiza as tabelas automaticamente          |
| `spring.jackson.time-zone`, `spring.web.locale` | timezone e idioma pt-BR                  |
| `app.jwt.secret`                    | chave do JWT (use a variável `JWT_SECRET`)           |
| `app.jwt.expiration-minutes`        | validade do token                                    |
| `spring.h2.console.enabled`         | liga/desliga o console web do H2                     |

---

## 7. Problemas comuns

| Sintoma | Causa / solução |
|---|---|
| `AccessDeniedException: /app/data` ao subir no podman | Use volume nomeado (`bikedata:/app/data`), como no compose atual, em vez de pasta local. |
| `401` em tudo | Token ausente, inválido ou expirado: faça login de novo. |
| `403` | Seu perfil não tem permissão para essa rota (ex.: CLIENTE em `/api/usuarios`). |
| `409` ao excluir | Registro em uso (cliente com locações, usuário com cliente, bicicleta com histórico). |
| Mudei `JWT_SECRET` e fui deslogado | Esperado: tokens antigos deixam de valer. |
| Console do H2 não conecta | Confira a URL exata `jdbc:h2:file:./data/bikerental`, usuário `sa` e senha vazia. |

---

