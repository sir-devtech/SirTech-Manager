# SirTech Manager

Controle mensal de uma empresa pequena de software: clientes, notas fiscais, despesas e o resumo do mês. A nota é emitida no site da prefeitura; o sistema importa o XML. Roda local, num comando.

Feito como sistema real de uso próprio e como estudo de Java. Os dados desta versão são fictícios.

## Stack

- Java 21, Spring Boot 4.1, Maven Wrapper
- PostgreSQL 18, Flyway
- Angular 22 com Angular Material
- Docker Compose, GitHub Actions
- Spring Security: login com sessão, senha em BCrypt e proteção CSRF
- Testes com JUnit 5, Mockito, Testcontainers, Vitest e Playwright; cobertura medida com JaCoCo; OSV-Scanner e `npm audit` no CI

## Como rodar

Com o Docker ligado:

```bash
docker compose up -d --build
```

Abra `http://localhost:4200` e entre com o login de demonstração, que a própria tela mostra:

| E-mail | Senha |
|---|---|
| `demo@sirtech.example` | `demo-sirtech-2026` |

A API fica em `http://localhost:8080` e o Swagger em `http://localhost:8080/swagger-ui.html` (a leitura é aberta; os pedidos exigem login). A demonstração tem três clientes fictícios e seis meses de histórico.

### Uso real, separado da demonstração

O uso real fica em outro arquivo, com outro banco e sem dados de exemplo. Copie `.env.example` para `.env`, preencha `REAL_POSTGRES_PASSWORD`, `SIRTECH_ADMIN_EMAIL` e `SIRTECH_ADMIN_SENHA`, pare a demonstração e rode:

```bash
docker compose down
docker compose -f docker-compose.real.yml up -d --build
```

Sem essas variáveis o comando nem começa. As portas 4200, 8080 e 5432 atendem só este computador (`127.0.0.1`).

## O que dá para ver

- **Entrar.** Sem sessão, nenhuma página abre e a API responde 401.
- **Painel.** Previsto, recebido, despesas e lucro do mês, com a cor dizendo o que cada número é, e um gráfico dos últimos seis meses.
- **Clientes.** Cadastro com validação de CPF e CNPJ.
- **Notas.** A nota nasce pendente, passa a emitida (na mão ou importando o XML) e a paga. Cancelar libera o mês para outra.
- **Financeiro.** O período (por padrão, os últimos 6 meses) como livro-caixa: demonstrativo, mês a mês, receita por cliente, despesas por categoria e notas em atraso.
- **Despesas.** Avulsas ou recorrentes. A recorrente é copiada para o mês seguinte e não duplica.
- **Backup.** Grava um arquivo SQL em `./backups`.

## Decisões

- **Regra de status na entidade.** `NotaFiscal.emitir`, `pagar` e `cancelar` consultam `StatusNota.podeTransicionarPara`. O controller não decide transição. Uma nota paga não volta atrás.
- **Módulos sem ciclo.** Ao editar um cliente, o `ClienteService` publica um evento e `nota/RealinhamentoDeNotas` ajusta as notas pendentes, na mesma transação. Antes isso era uma dependência circular escondida por `@Lazy` (ADR 0010).
- **Idempotência garantida pelo banco.** Um índice único parcial permite uma nota ativa por cliente em cada mês. A geração usa `INSERT ... ON CONFLICT DO NOTHING`, então rodar duas vezes não duplica. A nota cancelada fica fora do índice.
- **Geração no dia 1 e na inicialização.** Se a máquina ficou desligada, abrir o sistema põe o mês em dia e copia as despesas recorrentes do último mês que as tinha.
- **Dinheiro em `BigDecimal`.** A coluna é `numeric`. Centavo não passa por ponto flutuante.
- **Competência como `date`.** Guardada no dia 1 do mês, para o banco ordenar, e convertida para `YearMonth` no Java.
- **Login com sessão, não JWT.** Tela e API ficam na mesma origem. Cookie `HttpOnly` e `SameSite=Strict`, senha só em hash BCrypt, token CSRF no cabeçalho, troca do id da sessão ao entrar e bloqueio após 5 senhas erradas.
- **Um tratador de erro.** `GlobalExceptionHandler` devolve `ProblemDetail`: 400 na validação, 401 sem login, 403 sem CSRF, 404 quando não acha, 409 na constraint única, 422 na regra de negócio e 429 após muitas senhas erradas.
- **Teste contra o PostgreSQL real.** O Testcontainers sobe um PostgreSQL 18 descartável. O índice parcial e o `ON CONFLICT` não existem num banco em memória, então o teste que importa usa o banco de verdade.
- **Dois ambientes, o mesmo código.** `docker-compose.yml` é a demonstração, com seed e login fictícios. `docker-compose.real.yml` é o uso real, com projeto, banco e volume próprios, e exige senha do banco e do primeiro usuário. Segredo fica no `.env`, que não entra no Git.

O porquê de cada uma está em [`docs/adr`](docs/adr).

## Testes

```bash
cd backend && ./mvnw verify
cd frontend && npm ci && npx ng test --watch=false && npx ng build
bash scripts/e2e.sh   # sobe uma cópia descartável e abre um navegador de verdade
```

O `mvn verify` roda 110 testes, os de integração num PostgreSQL de teste pelo Docker. A tela tem 114 testes com Vitest, que cobrem as cinco páginas e o login. O `scripts/e2e.sh` roda 5 cenários no navegador (Playwright) contra o sistema inteiro, sem tocar na demonstração aberta. Os dois rodam no GitHub Actions a cada push, junto com o build de produção. A cobertura da API é de 90,5% das linhas, com um mínimo travado no build (88%).

## Fora de escopo

Emitir a nota no lugar da prefeitura, e-mail e deploy em nuvem.

## Documentação

