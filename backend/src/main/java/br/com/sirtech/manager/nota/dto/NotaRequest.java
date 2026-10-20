package br.com.sirtech.manager.nota.dto;

import java.math.BigDecimal;
import java.time.YearMonth;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

public record NotaRequest(
        @NotNull Long clienteId,
        @NotNull YearMonth competencia,
        @DecimalMin(value = "0.01") @Digits(integer = 10, fraction = 2) BigDecimal valor) {
}
