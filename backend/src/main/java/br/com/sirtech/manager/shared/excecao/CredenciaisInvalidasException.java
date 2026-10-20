package br.com.sirtech.manager.shared.excecao;

/**
 * E-mail ou senha errados. Vira HTTP 401. A mensagem é a mesma nos dois casos,
 * para não revelar se o e-mail existe.
 */
public class CredenciaisInvalidasException extends RuntimeException {

    public CredenciaisInvalidasException() {
        super("E-mail ou senha incorretos");
    }
}
