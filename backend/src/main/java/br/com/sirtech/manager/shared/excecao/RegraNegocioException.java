package br.com.sirtech.manager.shared.excecao;

/** Violação de uma regra de negócio. Vira HTTP 422 no {@code GlobalExceptionHandler}. */
public class RegraNegocioException extends RuntimeException {

    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}
