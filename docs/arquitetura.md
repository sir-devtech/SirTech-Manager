# Arquitetura

Como o SirTech Manager é montado: as peças, o caminho de um pedido, as regras do domínio e os dois ambientes.

## Visão geral

```
Navegador ──► nginx (tela Angular) ──/api──► Spring Boot (API) ──► PostgreSQL
                  porta 4200                     porta 8080           porta 5432
```

- **Tela:** Angular 22 com Angular Material, servida pelo nginx. O nginx também repassa `/api` para a API, então o navegador vê uma origem só (sem CORS).
- **API:** Java 21 e Spring Boot 4.1. Expõe REST em `/api` e documentação OpenAPI em `/swagger-ui.html`.
- **Banco:** PostgreSQL 18. O schema é versionado pelo Flyway (`db/migration`); o Hibernate só valida (`ddl-auto: validate`).
- **Empacotamento:** Docker Compose sobe os três serviços. As imagens são feitas em duas etapas: a primeira compila, a segunda leva só o resultado.

## Organização do código

O backend é organizado **por funcionalidade**, não por camada técnica ([ADR 0001](adr/0001-pacote-por-funcionalidade.md)):

| Pacote | Responsabilidade |
|---|---|
| `cliente` | Cadastro, validação de CPF/CNPJ, ativar e desativar |
| `nota` | Ciclo de vida da nota fiscal; `importacao` lê o XML da NFS-e |
| `despesa` | Despesas avulsas e recorrentes |
| `geracao` | Rotina do mês: notas pendentes e cópia das recorrentes |
| `resumo` / `financeiro` | Contas do mês e do período |
| `backup` | Cópia do banco com `pg_dump` |
| `acesso` | Login, sessão e regras de segurança |
| `shared` | Tratamento de erros, validação, relógio e conversores |

Dentro de cada pacote: entidade (regra do domínio), repository (acesso ao banco), service (caso de uso e transação), controller (HTTP) e `dto/` (contrato da API, em `record`). O frontend segue a mesma ideia: `core/` tem os serviços HTTP, a sessão, a guarda de rotas e o interceptor; `paginas/` tem uma pasta por tela.

## O caminho de um pedido

Exemplo: emitir uma nota (`PATCH /api/notas/{id}/emitir`).

1. A tela chama `NotaService.emitir` (Angular), que faz o pedido com o token CSRF.
2. O filtro de segurança confere a sessão e o token. Sem sessão: 401; sem token: 403.
3. O `NotaController` valida o corpo (`@Valid`) e chama o service.
4. O `NotaService` abre a transação, busca a nota e chama `nota.emitir(numero, data)`.
5. A **entidade** decide: `StatusNota.podeTransicionarPara(EMITIDA)`. Se não puder, lança `TransicaoInvalidaException`.
6. O `GlobalExceptionHandler` transforma qualquer exceção em `ProblemDetail` com o código HTTP certo. A tela mostra o `detail` num aviso.

## Regras do domínio

**Ciclo de vida da nota** ([ADR 0004](adr/0004-regra-de-status-no-dominio.md)):

```
PENDENTE ──emitir──► EMITIDA ──pagar──► PAGA
    │                    │
    └──cancelar──► CANCELADA ◄──cancelar──┘
```

- Emitir exige número e data; pagar exige data, e ela não pode ser anterior à emissão.
- Paga e cancelada não mudam mais.
- "Vencida" não é gravada: é calculada (emitida e com vencimento antes de hoje).
- Enquanto a nota está pendente, valor e vencimento acompanham o cliente. Editar o cliente publica o evento `ClienteAlterado`, e `RealinhamentoDeNotas` ajusta as pendentes na mesma transação ([ADR 0010](adr/0010-evento-de-dominio-no-lugar-da-dependencia-circular.md)).

**Uma nota ativa por cliente em cada mês**, garantida pelo banco com um índice único parcial (`WHERE status <> 'CANCELADA'`). A cancelada sai do índice e libera o mês ([ADR 0002](adr/0002-indice-unico-parcial.md)).

**Dinheiro** em `BigDecimal` no Java e `NUMERIC(12,2)` no banco ([ADR 0005](adr/0005-bigdecimal-para-dinheiro.md)). **Competência** guardada como o dia 1 do mês e usada como `YearMonth` ([ADR 0006](adr/0006-competencia-como-date.md)).

## Geração do mês

`GeracaoMensalService.gerar(competencia)` cria a nota pendente de cada cliente ativo e copia as despesas recorrentes do último mês que as tinha. Roda:

- todo dia 1 às 06:00 (`@Scheduled`);
- quando a API sobe (`ApplicationReadyEvent`), porque a máquina pode ter ficado desligada no dia 1;
- sob demanda, em `POST /api/notas/gerar`.

É **idempotente**: os inserts usam `INSERT ... ON CONFLICT DO NOTHING` apoiados nos índices únicos parciais, então rodar duas vezes não duplica nada ([ADR 0003](adr/0003-geracao-idempotente-e-na-inicializacao.md)).

## Importação do XML da NFS-e

A nota é emitida no site da prefeitura; o sistema importa o XML em `POST /api/notas/importar`.

- O leitor procura as tags pelo nome e aceita o padrão ABRASF e o padrão nacional.
- DTD e entidades externas são recusados, para um XML malicioso não ler arquivos da máquina.
- O cliente é achado pelo CPF/CNPJ do tomador. Cliente desconhecido ou inativo: 422.
- A nota pendente do mês recebe número, data e valor do XML. Importar o mesmo arquivo de novo não duplica; outro número para o mesmo mês: 409.
- O XML original fica guardado no banco, nunca no repositório.

## Ambientes

| | Demonstração | Uso real |
|---|---|---|
| Arquivo | `docker-compose.yml` | `docker-compose.real.yml` |
| Perfil Spring | `dev` (carrega `db/seed`) | padrão, sem seed |
| Dados | Três clientes fictícios e seis meses de histórico | Vazio |
| Login | `demo@sirtech.example`, mostrado na tela | Criado pelas variáveis do `.env` |
| Banco | Volume `pgdata-demo` | Projeto e volume próprios |

Os dois usam portas presas a `127.0.0.1`. O uso real não sobe sem as senhas no `.env`. Segredos e dados reais nunca entram no Git.

## Integração contínua

O GitHub Actions roda quatro trabalhos a cada push:

| Trabalho | O que faz |
|---|---|
| API | `./mvnw verify`: testes unitários e de integração com PostgreSQL real e o mínimo de cobertura |
| Tela | `npm audit`, testes com Vitest e build de produção |
| Dependências | OSV-Scanner em `pom.xml` e nos `package-lock.json` (também toda segunda-feira) |
| Ponta a ponta | Sobe o sistema inteiro e roda os cenários do Playwright; só depois que API e tela passam |

Os detalhes de cada tema estão em [segurança](seguranca.md), [testes](testes.md) e [limites e evolução](limites.md).
