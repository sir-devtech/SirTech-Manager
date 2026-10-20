package br.com.sirtech.manager.acesso.dto;

/**
 * Login de demonstração mostrado na tela de entrar. Só vem preenchido no perfil dev;
 * no uso real, {@code ativa} é false e e-mail e senha vêm nulos.
 */
public record Demonstracao(boolean ativa, String email, String senha) {

    public static Demonstracao desligada() {
        return new Demonstracao(false, null, null);
    }
}
