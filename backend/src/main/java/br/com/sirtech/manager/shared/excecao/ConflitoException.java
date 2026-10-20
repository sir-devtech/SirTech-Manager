package br.com.sirtech.manager.shared.excecao;

/** Conflito de regra que não vem do banco. Vira HTTP 409. */
public class ConflitoException extends RuntimeException {

    public ConflitoException(String mensagem) {
        super(mensagem);
    }
}
