package br.com.sirtech.manager.nota.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;

public record PagarNotaRequest(@NotNull LocalDate dataPagamento) {
}
