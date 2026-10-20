# ADR 0010 — Evento de domínio no lugar da dependência circular

**Status:** aceito · **Fase:** refinamento

## Contexto
Ao editar um cliente, as notas **pendentes** dele precisam acompanhar o novo valor e o novo dia de vencimento. `ClienteService` chamava `NotaService.realinharPendentes(...)`. Só que `NotaService` já depende de `ClienteService` (para achar o cliente ao criar uma nota). Isso formava um ciclo, e o Spring só subia com `@Lazy` num dos construtores.

`@Lazy` esconde o problema: os dois módulos continuam amarrados um ao outro. Um revisor experiente lê isso como responsabilidade no lugar errado.

## Decisão
O módulo `cliente` publica um **evento de domínio** e não conhece mais as notas.

- `ClienteService.atualizar` publica `ClienteAlterado(cliente)` com `ApplicationEventPublisher`.
- `nota/RealinhamentoDeNotas` assina o evento com `@EventListener` e chama `NotaService.realinharPendentes`.
- O ouvinte é **síncrono**: roda na mesma thread e na mesma transação de quem publicou. Se o realinhamento falhar, a edição do cliente também volta atrás.

A dependência agora vai em um sentido só: `nota` conhece `cliente`, e `cliente` não conhece `nota`.

## Alternativas consideradas
- **Manter `@Lazy`:** funciona, mas o acoplamento continua e o próximo módulo repete o truque.
- **Mover `realinharPendentes` para `ClienteService`:** o cliente passaria a mexer em notas, que não são dele.
- **`@TransactionalEventListener(AFTER_COMMIT)`:** roda depois do commit, fora da transação. Se falhasse, o cliente já estaria salvo com as notas desatualizadas. Para esta regra, atomicidade vale mais que isolamento.
- **Mensageria (fila):** pesado demais para um sistema local de uma pessoa (YAGNI).

## Consequências
- O ciclo sumiu e o `@Lazy` saiu do código.
- Novos assuntos podem reagir à mudança do cliente sem editar `ClienteService`.
- O fluxo fica menos explícito: quem lê `atualizar` não vê direto quem reage. Por isso o evento tem Javadoc e o fluxo tem teste de ponta a ponta (`ClienteApiIT`: editar o cliente atualiza a nota pendente).
- Padrões envolvidos: Observer (evento) e inversão de dependência.
