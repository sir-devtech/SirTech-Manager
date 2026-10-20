package br.com.sirtech.manager.shared.config;

import java.time.Clock;
import java.time.ZoneId;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Expõe o "relógio" da aplicação como bean. Quem precisa saber "hoje" recebe o Clock por injeção,
 * e os testes podem fixar uma data com {@link Clock#fixed}.
 */
@Configuration
public class ClockConfig {

    @Bean
    Clock clock(@Value("${sirtech.zona}") String zona) {
        return Clock.system(ZoneId.of(zona));
    }
}
