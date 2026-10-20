package br.com.sirtech.manager.despesa.dto;

import java.math.BigDecimal;
import java.time.YearMonth;

import br.com.sirtech.manager.despesa.CategoriaDespesa;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record DespesaRequest(
        @NotBlank @Size(max = 150) String descricao,
        @NotNull CategoriaDespesa categoria,
        @NotNull @DecimalMin(value = "0.01") @Digits(integer = 10, fraction = 2) BigDecimal valor,
        @NotNull YearMonth competencia,
        @NotNull Boolean recorrente) {
}
