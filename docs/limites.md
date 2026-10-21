# Limites e evolução

O sistema cabe numa máquina e atende uma pessoa. Estas são as escolhas feitas para esse tamanho e o que mudaria com mais usuários ou mais volume.

| Hoje | Com mais usuários ou mais volume | Por que espera |
|---|---|---|
| Login com sessão, um papel só, bloqueio em memória | Papéis (dona, contador só leitura), bloqueio no banco ou em cache compartilhado, segundo fator | Há uma usuária e um processo |
| Roda local, sem HTTPS | Deploy com HTTPS, domínio e proxy reverso | O uso é de uma pessoa, no próprio computador |
| Backup manual por botão | Backup agendado, cifrado, fora da máquina e com teste de restauração | O volume é pequeno |
| Resumo somado em memória | Agregação no banco ou visão materializada, se a lista passar de milhares | Com poucas notas por mês, somar em memória é mais simples de testar |
| Geração no dia 1 e na inicialização | Fila, com retentativa e registro do que falhou | Não há pico nem outro serviço para avisar |
| Sem observabilidade | Endpoint de saúde, métricas, log estruturado e rastreio de pedido | O log do Spring resolve enquanto há um usuário |
| XML conferido pelo CNPJ do prestador | Validar a assinatura digital da nota | O risco hoje é importar o arquivo errado, e o CNPJ cobre isso |

## O que não muda com o tamanho

A regra de situação da nota na entidade, a idempotência garantida pelo índice único parcial do banco e os testes contra o PostgreSQL real. Essas escolhas valem para qualquer escala.

## Fora do escopo

Emitir a nota no lugar da prefeitura, pagar boleto ou cartão, enviar e-mail e publicar na nuvem.
