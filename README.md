# SirTech Manager

Controle mensal de uma empresa pequena de software: clientes, notas fiscais, despesas e o resumo do mês. A nota é emitida no site da prefeitura; o sistema importa o XML. Roda local, num comando.

Feito como sistema real de uso próprio e como estudo de Java. Os dados desta versão são fictícios.

## Stack

- Java 21, Spring Boot 4.1, Maven Wrapper
- PostgreSQL 18, Flyway
- Angular 22 com Angular Material
- Docker Compose, GitHub Actions
- Testes com JUnit 5, Mockito e Testcontainers

## Como rodar

Com o Docker ligado:

```bash
docker compose up -d --build
```

Abra `http://localhost:4200`. A API fica em `http://localhost:8080` e o Swagger em `http://localhost:8080/swagger-ui.html`.

Esse comando sobe a demonstração, com três clientes fictícios e seis meses de histórico. Para o uso real, sem dados de exemplo e com volume separado, copie `.env.example` para `.env`, preencha `REAL_POSTGRES_PASSWORD` e rode:

```bash
docker compose --profile real up -d --build
```

Sem essa senha o banco real não é criado. Pare um ambiente antes de subir o outro: os dois usam as portas 4200, 8080 e 5432, e todas atendem só este computador (`127.0.0.1`).

## O que dá para ver

- **Painel.** Previsto, recebido, despesas e lucro do mês, com a cor dizendo o que cada número é, e um gráfico dos últimos seis meses.
- **Clientes.** Cadastro com validação de CPF e CNPJ.
- **Notas.** A nota nasce pendente, passa a emitida (na mão ou importando o XML) e a paga. Cancelar libera o mês para outra.
- **Despesas.** Avulsas ou recorrentes. A recorrente é copiada para o mês seguinte e não duplica.
- **Backup.** Grava um arquivo SQL em `./backups`.

## Decisões

- **Regra de status na entidade.** `NotaFiscal.emitir`, `pagar` e `cancelar` consultam `StatusNota.podeTransicionarPara`. O controller não decide transição. Uma nota paga não volta atrás.
- **Idempotência garantida pelo banco.** Um índice único parcial permite uma nota ativa por cliente em cada mês. A geração usa `INSERT ... ON CONFLICT DO NOTHING`, então rodar duas vezes não duplica. A nota cancelada fica fora do índice.
- **Geração no dia 1 e na inicialização.** Se a máquina ficou desligada, abrir o sistema põe o mês em dia e copia as despesas recorrentes do último mês que as tinha.
- **Dinheiro em `BigDecimal`.** A coluna é `numeric`. Centavo não passa por ponto flutuante.
- **Competência como `date`.** Guardada no dia 1 do mês, para o banco ordenar, e convertida para `YearMonth` no Java.
- **Um tratador de erro.** `GlobalExceptionHandler` devolve `ProblemDetail`: 400 na validação, 404 quando não acha, 409 na constraint única e 422 na regra de negócio.
- **Teste contra o PostgreSQL real.** O Testcontainers sobe um PostgreSQL 18 descartável. O índice parcial e o `ON CONFLICT` não existem num banco em memória, então o teste que importa usa o banco de verdade.
- **Dois ambientes, o mesmo código.** O perfil `dev` carrega o seed fictício. O perfil real não carrega seed nenhum. Cada um tem o seu volume. Segredo fica no `.env`, que não entra no Git.

O porquê de cada uma está em [`docs/adr`](docs/adr).

## Testes

```bash
cd backend && ./mvnw verify
cd frontend && npm ci && npx ng test --watch=false && npx ng build
```

O `mvn verify` sobe o PostgreSQL de teste pelo Docker. Os mesmos testes rodam no GitHub Actions, em cada push.

## Fora de escopo

Emitir a nota no lugar da prefeitura, login, e-mail e deploy em nuvem.

## Documentação

