# ADR 0003 — Geração idempotente e na inicialização

**Status:** aceito · **Fase:** 4

## Contexto
Todo mês cada cliente ativo precisa de uma nota pendente, e os gastos marcados como recorrentes precisam reaparecer. A rotina pode ser disparada pelo relógio (dia 1, 06:00), pela subida do sistema e por um botão. Essas três portas podem se cruzar.

## Decisão
O insert da nota e o insert da despesa recorrente usam `ON CONFLICT ... DO NOTHING` em cima dos índices únicos parciais que já existem. A resposta conta só as linhas que entraram de fato.

A despesa copiada vem da última competência anterior que tenha recorrentes, não apenas do mês imediatamente anterior.

O job usa `@Scheduled`. A subida usa `ApplicationReadyEvent`. Os dois podem ser desligados por property, e os testes desligam.

## Alternativas consideradas
- **`if (!existe) salva`:** perde a corrida entre o job e o botão.
- **Copiar só do mês anterior:** um mês com o PC desligado zera a cadeia de recorrentes.
- **Fila ou scheduler externo:** desnecessário para um sistema local de dois clientes.

## Consequências
- Rodar a geração N vezes deixa uma nota ativa por cliente/mês e uma cópia de cada recorrente.
- Nota cancelada continua podendo ser recriada, porque o índice ignora `CANCELADA`.
- O SQL nativo conhece o nome das colunas. Uma mudança de schema precisa atualizar essa query.
