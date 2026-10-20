package br.com.sirtech.manager.shared.config;

import java.time.YearMonth;

import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** Converte o parâmetro {@code ?competencia=2026-10} em {@link YearMonth}. Formato errado vira 400. */
@Configuration
public class AnoMesWebConfig implements WebMvcConfigurer {

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(String.class, YearMonth.class, YearMonth::parse);
    }
}
