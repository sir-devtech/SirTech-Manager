package br.com.sirtech.manager.cliente.dto;

import java.math.BigDecimal;

import br.com.sirtech.manager.cliente.Produto;
import br.com.sirtech.manager.shared.validacao.CpfOuCnpj;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ClienteRequest(
        @NotBlank @Size(max = 150)
        String nome,

        @NotBlank @CpfOuCnpj
        String documento,

        @Email @Size(max = 150)
        String email,

        @NotNull
        Produto produto,

        @NotNull @DecimalMin("0.01") @Digits(integer = 10, fraction = 2)
        BigDecimal valorMensal,

        @NotNull @Min(1) @Max(28)
        Integer diaVencimento) {
}
