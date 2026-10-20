package br.com.sirtech.manager.acesso.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EntrarRequest(
        @NotBlank @Email @Size(max = 150)
        String email,

        @NotBlank @Size(max = 200)
        String senha) {

    /** Nunca imprime a senha em log ou em mensagem de erro. */
    @Override
    public String toString() {
        return "EntrarRequest[email=" + email + ", senha=***]";
    }
}
