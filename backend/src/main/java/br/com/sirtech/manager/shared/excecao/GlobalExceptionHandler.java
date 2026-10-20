package br.com.sirtech.manager.shared.excecao;

import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Traduz exceções em respostas HTTP no formato ProblemDetail (RFC 7807).
 *
 * <p>A classe-mãe {@link ResponseEntityExceptionHandler} já trata os erros "do Spring"
 * (JSON malformado, método não suportado, validação...) devolvendo ProblemDetail.
 *
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    // TODO 1 (400): sobrescrever handleMethodArgumentNotValid e incluir a propriedade "erros"
    //               (mapa campo -> mensagem) no ProblemDetail.

    // TODO 2 (404): tratar RecursoNaoEncontradoException.

    // TODO 3 (422): tratar RegraNegocioException.

    // TODO 4 (409): tratar DataIntegrityViolationException, identificando a constraint
    //               "uk_cliente_documento" para devolver uma mensagem clara.
}
