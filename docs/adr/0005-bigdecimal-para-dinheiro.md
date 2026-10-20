# ADR 0005 — BigDecimal para dinheiro

**Status:** aceito · **Fase:** 1

## Contexto
`double` e `float` são ponto flutuante binário: `0.1 + 0.2 == 0.30000000000000004`. Em finanças, centavos perdidos somam erro.

## Decisão
Todo valor monetário é `BigDecimal` em Java e `NUMERIC(12,2)` no PostgreSQL. Comparações usam `compareTo` (não `equals`, que considera a escala: `1.0` ≠ `1.00`). Na entrada, `@Digits(integer=10, fraction=2)` limita a 2 casas.

## Alternativas consideradas
- **`long` em centavos:** rápido e exato, mas exige converter em toda borda (API, tela) e é fácil errar.
- **`double`:** descartado por imprecisão.

## Consequências
- Exatidão garantida.
- Código um pouco mais verboso (`a.add(b)` em vez de `a + b`).
