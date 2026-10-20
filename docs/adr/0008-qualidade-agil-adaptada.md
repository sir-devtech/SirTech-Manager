# ADR 0008 — Qualidade ágil adaptada ao tamanho

**Status:** aceito · **Fase:** transversal

## Contexto
Metodologia ágil e normas ISO (12207 ciclo de vida, 29119 testes, 27001 segurança) parecem brigar por causa da documentação. O manifesto prefere software funcionando a documentação abrangente. A própria 12207 manda adaptar o processo ao projeto (tailoring). Este sistema é interno, local, com uma pessoa e cerca de duas notas por mês.

## Decisão
Seguir o espírito dessas normas, sem buscar certificado e sem criar papéis de documentação.

- Cada etapa é um incremento: código, teste (`mvn verify`) e um registro curto do que mudou.
- Decisão que muda o desenho vira um ADR. O resto não vira manual.
- Teste cobre o risco de verdade: dinheiro, status da nota, índice do banco, segredo fora do comando.
- Dado real, backup e XML de cliente ficam fora do Git. Demo e uso real usam volumes separados.
- A arquitetura continua por funcionalidade. A regra fica na entidade, o caso de uso no service, e HTTP, XML e `pg_dump` na borda.

## Alternativas consideradas
- **Certificar ISO e manter os artefatos da norma** (folha de sessão, relatório diário, equipe só de documentação): peso de empresa grande em cima de um sistema de duas notas por mês.
- **Não escrever nada:** a decisão se perde, e quem chega depois não sabe por que o código é como é.

## Consequências
- Fica registrado o que foi adaptado e o que foi deixado de fora de propósito.
- O fluxo de cada fase continua o mesmo: implementar, testar, registrar, parar para revisão.
