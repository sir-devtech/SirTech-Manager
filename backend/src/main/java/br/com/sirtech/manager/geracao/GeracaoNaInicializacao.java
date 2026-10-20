package br.com.sirtech.manager.geracao;

import java.time.Clock;
import java.time.YearMonth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * O computador pode estar desligado no dia 1. Ao abrir o sistema, gera o mês atual.
 * A segunda vez não duplica.
 */
@Component
@ConditionalOnProperty(name = "sirtech.geracao.inicializacao.habilitado", havingValue = "true")
public class GeracaoNaInicializacao {

    private static final Logger log = LoggerFactory.getLogger(GeracaoNaInicializacao.class);

    private final GeracaoMensalService service;
    private final Clock clock;

    public GeracaoNaInicializacao(GeracaoMensalService service, Clock clock) {
        this.service = service;
        this.clock = clock;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void aoSubir() {
        YearMonth competencia = YearMonth.now(clock);
        ResultadoGeracao resultado = service.gerar(competencia);
        log.info("Geração na inicialização de {}: {} notas, {} despesas",
                competencia, resultado.notasCriadas(), resultado.despesasReplicadas());
    }
}
