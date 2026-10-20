package br.com.sirtech.manager.shared.tempo;

import java.time.LocalDate;
import java.time.YearMonth;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * No Java a competência é um mês ({@code 2026-10}). No banco é o dia 1 desse mês,
 * porque {@code date} ordena, compara e indexa sem conversão extra.
 */
@Converter(autoApply = true)
public class ConversorAnoMes implements AttributeConverter<YearMonth, LocalDate> {

    @Override
    public LocalDate convertToDatabaseColumn(YearMonth anoMes) {
        return anoMes == null ? null : anoMes.atDay(1);
    }

    @Override
    public YearMonth convertToEntityAttribute(LocalDate data) {
        return data == null ? null : YearMonth.from(data);
    }
}
