# ADR 0006 — Competência como date

**Status:** aceito · **Fase:** 2

## Contexto
A competência é um mês (`2026-10`). O banco precisa ordenar, comparar e indexar esse valor junto com o cliente.

## Decisão
Coluna `competencia date`, sempre o dia 1 do mês. No Java o campo continua `YearMonth`, traduzido por `ConversorAnoMes` (`autoApply`). Na URL, `?competencia=2026-10` vira `YearMonth` por um conversor do Spring. Texto fora do formato vira 400.

## Alternativas consideradas
- **`char(7)` com `AAAA-MM`:** legível no SQL, mas ordenar e indexar é menos natural e o formato inválido entra fácil.
- **Duas colunas ano e mês:** mais joins e mais chance de mês 13.

## Consequências
- O vencimento é `competencia.atDay(diaVencimento)`. Como o dia do cliente só vai de 1 a 28, fevereiro nunca quebra.
- Quem lê o SQL vê `2026-10-01` e precisa saber que isso significa o mês, não o dia da nota.
