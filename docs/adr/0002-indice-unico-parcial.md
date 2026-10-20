# ADR 0002 — Índice único parcial para a nota do mês

**Status:** aceito · **Fase:** 2

## Contexto
Um cliente não pode ter duas notas ativas na mesma competência. Uma nota cancelada precisa liberar aquele mês para outra nota.

## Decisão
Índice único parcial no PostgreSQL:

```sql
CREATE UNIQUE INDEX uk_nota_cliente_competencia
    ON nota_fiscal (cliente_id, competencia)
    WHERE status <> 'CANCELADA';
```

A API traduz a violação para 409, pela mesma tabela de mensagens do documento duplicado.

## Alternativas consideradas
- **`UNIQUE (cliente_id, competencia)` comum:** simples, mas a nota cancelada ocuparia o mês para sempre.
- **Conferir com `exists` no Java antes de salvar:** falha se dois pedidos chegam ao mesmo tempo. Os dois passam no `exists` e os dois inserem.

## Consequências
- A garantia mora no banco, inclusive para o job da Fase 4.
- O Hibernate não declara esse índice. Ele vive só na migration.
