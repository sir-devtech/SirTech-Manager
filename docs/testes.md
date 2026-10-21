# Testes

A estratégia segue a pirâmide: muitos testes rápidos na base e poucos testes caros no topo. A decisão está no [ADR 0007](adr/0007-estrategia-de-testes.md).

| Nível | Ferramentas | Quantos | O que cobre |
|---|---|---|---|
| Unitário | JUnit 5, Mockito, AssertJ | 80 | Regras do domínio, cálculos, validadores, services com dependências trocadas por dublês |
| Integração | Spring Boot Test, Testcontainers (PostgreSQL 18) | 30 | Repositórios, índices e constraints do banco, fluxos HTTP completos, segurança com os filtros ligados |
| Tela | Vitest, TestBed | 114 | As cinco páginas, os formulários, a sessão, a guarda de rotas e o interceptor |
| Ponta a ponta | Playwright (Chromium) | 5 | O fluxo principal no navegador, contra o sistema inteiro |

## Backend

- **Testes unitários (`*Test`)** rodam no `mvn test`, sem banco. Exemplo: `StatusNotaTest` percorre todas as transições de situação da nota.
- **Testes de integração (`*IT`)** rodam no `mvn verify` contra um **PostgreSQL real** e descartável, criado pelo Testcontainers. Um banco em memória não reproduziria o índice único parcial nem o `ON CONFLICT`. Exemplo: `NotaRepositoryIT` prova que duas notas ativas no mesmo mês são recusadas pelo próprio banco.
- O container é iniciado uma vez e compartilhado entre as classes (`AbstractIntegrationTest`), porque o Spring reaproveita o contexto entre elas.
- O "hoje" vem de um `Clock` injetado, então os testes que dependem de data são determinísticos.
- `AcessoIT` prova o login de ponta a ponta: sem sessão dá 401, sem token CSRF dá 403, a sexta senha errada dá 429, sair invalida a sessão e o banco guarda só o hash.

### Cobertura

O JaCoCo junta a cobertura dos testes unitários e de integração: **90,5% das linhas e 71,8% dos ramos**. O build falha se as linhas caírem abaixo de 88% ou os ramos abaixo de 68%. O que fica sem cobertura é código que só faz sentido rodando de verdade (a classe `main`, o agendador e o executor do `pg_dump`).

O CI escreve a porcentagem no resumo de cada execução e guarda o relatório HTML como artefato.

## Frontend

Cada teste monta uma página isolada, com a API trocada por um dublê, e confere o que aparece na tela ou o que foi pedido à API. Exemplos:

- Notas: os botões seguem a situação (pendente: Emitir e Cancelar; emitida: Pagar e Cancelar; paga e cancelada: nenhum).
- Clientes: a busca acha por nome e por CNPJ, com ou sem máscara; o formulário recusa CPF/CNPJ inválido e dia de vencimento fora de 1 a 28.
- Painel: o card de despesas mostra a fração do previsto e não divide por zero; o aviso de atraso só aparece com nota vencida.
- Entrar: um parâmetro `voltar` que aponta para outro site é ignorado.

## Ponta a ponta

`bash scripts/e2e.sh` sobe uma **cópia descartável** do sistema (outro nome de projeto, outros volumes e outras portas), roda o Chromium num container ligado à rede do Compose e derruba tudo no fim (`down -v`). A demonstração que estiver aberta não é afetada.

Cenários: senha errada; login de demonstração abrindo o painel com os valores esperados; emissão de uma nota; financeiro com o demonstrativo de seis meses; sair encerrando a sessão. Em caso de falha, o Playwright guarda vídeo e trace, e o CI os publica como artefato.

O mesmo script, com `bash scripts/e2e.sh prints`, refaz os prints e o GIF do README.

## Validação dos próprios testes

Para confirmar que os testes realmente protegem o sistema, defeitos foram plantados no código de propósito e depois desfeitos:

| Defeito plantado | Pego por |
|---|---|
| Botão Pagar aparecendo em nota já paga | teste da página de Notas |
| Card de despesas sem dividir pelo previsto | teste do Painel |
| Busca de clientes ignorando o documento | dois testes da página de Clientes |
| Botão Pagar nunca aparecendo | teste de ponta a ponta (com vídeo e trace da falha) |

## Como rodar

```bash
cd backend && ./mvnw verify                                       # unitários + integração + cobertura
cd frontend && npm ci && npx ng test --watch=false && npx ng build # tela
bash scripts/e2e.sh                                               # ponta a ponta
```

O `mvn verify` e o `e2e.sh` precisam do Docker ligado.
