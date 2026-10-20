package br.com.sirtech.manager.geracao;

import java.time.Clock;
import java.time.YearMonth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Dia 1, às 06:00, no fuso de São Paulo. */
@Component
public class GeracaoMensalJob {

    private static final Logger log = LoggerFactory.getLogger(GeracaoMensalJob.class);

    private final GeracaoMensalService service;
    private final Clock clock;

    public GeracaoMensalJob(GeracaoMensalService service, Clock clock) {
        this.service = service;
        this.clock = clock;
    }

    @Scheduled(cron = "0 0 6 1 * *", zone = "America/Sao_Paulo")
    public void gerarMesAtual() {
        YearMonth competencia = YearMonth.now(clock);
        ResultadoGeracao resultado = service.gerar(competencia);
        log.info("Geração agendada de {}: {} notas, {} despesas",
                competencia, resultado.notasCriadas(), resultado.despesasReplicadas());
    }
}
