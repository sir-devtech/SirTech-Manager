# ADR 0001 — Pacotes por funcionalidade

**Status:** aceito · **Fase:** 1

## Contexto
O código Spring costuma ser organizado por camada técnica (`controller/`, `service/`, `repository/`). Em projetos que crescem, uma mudança em "notas" acaba espalhada por 4 pastas.

## Decisão
Organizar por **funcionalidade**: `cliente/`, `nota/`, `despesa/`, `geracao/`, `resumo/`, `backup/`, cada uma com entidade, repository, service, controller e `dto/`. O que é transversal fica em `shared/`.

## Alternativas consideradas
- **Por camada:** familiar, mas com baixa coesão. Tudo é `public` porque as classes de uma feature estão em pacotes diferentes.
- **Hexagonal/Clean completa (ports & adapters):** desacoplamento máximo, mas exagero (YAGNI) para um sistema com 4 entidades.

## Consequências
- Alta coesão: tudo de "cliente" está num lugar só.
- Facilita extrair um módulo no futuro.
- Exige disciplina para que as dependências entre módulos passem pelos *services* (ex.: `nota` usa `ClienteService.buscarEntidade`) e não por acesso direto a repositories alheios.
