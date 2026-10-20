# ADR 0004 — Regra de status no domínio

**Status:** aceito · **Fase:** 2

## Contexto
A nota passa por pendente, emitida, paga e cancelada. Paga não volta. Colocar esses `if` no controller repetiria a regra em emitir, pagar e cancelar.

## Decisão
`StatusNota.podeTransicionarPara` concentra os caminhos. `NotaFiscal.emitir`, `pagar` e `cancelar` consultam o enum e lançam `TransicaoInvalidaException`, que é uma `RegraNegocioException` e portanto já vira HTTP 422.

## Alternativas consideradas
- **`if` no service ou no controller:** funciona no começo e se perde quando surge mais um endpoint.
- **Biblioteca de máquina de estados:** pesada demais para quatro status.

## Consequências
- A matriz 4×4 cabe num teste parametrizado.
- Um status novo se adiciona no enum, sem mexer no controller.
