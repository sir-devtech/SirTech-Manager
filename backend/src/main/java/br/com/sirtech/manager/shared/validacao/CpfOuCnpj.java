package br.com.sirtech.manager.shared.validacao;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

/**
 * Valida CPF (11 dígitos) ou CNPJ (14 dígitos), com ou sem máscara, conferindo os dígitos verificadores.
 * Valor nulo é considerado válido: combine com {@code @NotBlank} quando o campo for obrigatório.
 */
@Documented
@Constraint(validatedBy = CpfOuCnpjValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
public @interface CpfOuCnpj {

    String message() default "CPF ou CNPJ inválido";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
