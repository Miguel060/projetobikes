# API de Aluguel de Bicicletas

Spring Boot 3 · Java 21 · H2 · JWT · Swagger · frontend estático simples.

## Rodar com Docker

```bash
docker compose up --build
```

- Frontend: http://localhost:8080
- Swagger: http://localhost:8080/swagger-ui.html

## Rodar sem Docker

```bash
mvn spring-boot:run
```

## Usuários de demonstração

| Usuário | Senha      | Perfil  |
|---------|------------|---------|
| admin   | admin123   | ADMIN   |
| cliente | cliente123 | CLIENTE |

## Regras de negócio

- Só é possível alugar bicicleta `DISPONIVEL`; ao alugar vira `ALUGADA`, ao devolver volta a `DISPONIVEL`.
- Cada cliente pode ter 1 locação ativa por vez.
- Valor da locação: R$ 5,00 por hora iniciada (mínimo 1 hora).
- ADMIN: acesso total. CLIENTE: vê bicicletas, aluga/devolve e vê apenas as próprias locações.
- Cliente é vinculado a um Usuário com perfil CLIENTE (crie o usuário primeiro em `/api/usuarios`).
- Auditoria automática (data/usuário de criação e modificação) em Usuario e Cliente.
