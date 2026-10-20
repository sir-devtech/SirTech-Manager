# ADR 0007 — Estratégia de testes (pirâmide + Testcontainers)

**Status:** aceito · **Fase:** 1 (evolui nas fases seguintes)

## Contexto
Regras importantes deste sistema vivem **no banco** (UNIQUE, CHECK, índice único parcial, `ON CONFLICT`). Um banco em memória (H2) não reproduz isso fielmente.

## Decisão
| Nível | Ferramenta | Sufixo | Quando roda |
|---|---|---|---|
| Unitário | JUnit 5 + Mockito + AssertJ | `*Test` | `mvn test` (Surefire) |
| API (camada web) | `@WebMvcTest` + MockMvc | `*Test` | `mvn test` |
| Integração | `@SpringBootTest` + Testcontainers (PostgreSQL 16) | `*IT` | `mvn verify` (Failsafe) |

- Um único container por execução (padrão *singleton container*) para os ITs ficarem rápidos.
- O tempo é injetado (`Clock`) para testes de data serem determinísticos.

## Alternativas consideradas
- **H2:** rápido, mas não suporta o índice parcial com a mesma semântica, e passaria testes que falham em produção.
- **Banco local compartilhado:** testes dependeriam do estado da máquina.

## Consequências
- Testes de integração exigem Docker (local e no CI: o GitHub Actions já tem).
- Alta confiança: o que passa no teste roda igual em produção.
