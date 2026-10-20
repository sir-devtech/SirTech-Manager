# SirTech Manager

Sistema interno de gestão da **Sir Tech Solutions** (SaaS): clientes, notas fiscais, despesas e resumo financeiro mensal. Roda 100% local.

> 🚧 Em construção, fase a fase. Veja o [plano](docs/PLANO.md) e o [diário de aprendizado](docs/README.md).

**Stack:** Java 21 · Spring Boot 3 · PostgreSQL 16 · Flyway · Testcontainers · Angular · Docker Compose

## Rodando (fase atual)
```bash
docker compose up -d db
cd backend
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```
Swagger: http://localhost:8080/swagger-ui.html

## Testes
```bash
cd backend
./mvnw verify   # requer Docker ligado (Testcontainers)
```
