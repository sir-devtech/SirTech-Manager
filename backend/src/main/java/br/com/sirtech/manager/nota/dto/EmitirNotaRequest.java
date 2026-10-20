package br.com.sirtech.manager.nota.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record EmitirNotaRequest(
        @NotBlank String numero,
        @NotNull LocalDate dataEmissao) {
}
