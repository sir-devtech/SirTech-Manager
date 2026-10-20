package br.com.sirtech.manager.cliente;

/**
 * Evento de domínio: os dados de cobrança de um cliente mudaram (valor mensal, dia de vencimento...).
 *
 * <p>O cliente avisa e não sabe quem escuta. Quem precisa reagir (hoje, as notas pendentes) assina o
 * evento. Assim o módulo {@code cliente} não depende do módulo {@code nota}, e a dependência
 * circular que existia entre os dois services sumiu.
 */
public record ClienteAlterado(Cliente cliente) {
}
