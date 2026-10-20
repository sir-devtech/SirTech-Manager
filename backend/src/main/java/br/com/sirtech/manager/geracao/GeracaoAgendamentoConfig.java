package br.com.sirtech.manager.geracao;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/** Liga o agendador. Nos testes a property fica false e o job não dispara. */
@Configuration
@EnableScheduling
@ConditionalOnProperty(name = "sirtech.geracao.agendamento.habilitado", havingValue = "true")
public class GeracaoAgendamentoConfig {
}
