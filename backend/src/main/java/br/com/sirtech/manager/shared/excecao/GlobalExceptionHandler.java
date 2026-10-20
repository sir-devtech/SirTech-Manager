package br.com.sirtech.manager.shared.excecao;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Traduz exceções em respostas HTTP no formato ProblemDetail (RFC 7807).
 *
 * <p>A classe-mãe {@link ResponseEntityExceptionHandler} já trata os erros "do Spring"
 * (JSON malformado, método não suportado, validação...) devolvendo ProblemDetail.
 *
 * <p>Cinco respostas saem daqui: 400 (campo inválido), 404 (não encontrado),
 * 409 (dado repetido no banco), 422 (regra de negócio) e 500 (backup falhou).
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    /** Nome da trava no banco → mensagem que a API devolve. Novas travas entram aqui. */
    private static final Map<String, String> MENSAGENS_DE_CONFLITO = Map.of(
            "uk_cliente_documento", "Já existe um cliente com este CPF/CNPJ",
            "uk_nota_cliente_competencia", "Já existe uma nota ativa deste cliente nesta competência",
            "uk_despesa_recorrente", "Já existe uma despesa recorrente igual nesta competência");

    private static final String CONFLITO_GENERICO =
            "Os dados informados conflitam com um registro já existente";

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        Map<String, String> erros = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(erro ->
                erros.put(erro.getField(), erro.getDefaultMessage()));

        ProblemDetail problema = ProblemDetail.forStatusAndDetail(status, "Um ou mais campos estão inválidos");
        problema.setTitle("Dados inválidos");
        problema.setProperty("erros", erros);

        return handleExceptionInternal(ex, problema, headers, status, request);
    }

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    ProblemDetail tratarNaoEncontrado(RecursoNaoEncontradoException ex) {
        return problema(HttpStatus.NOT_FOUND, "Recurso não encontrado", ex.getMessage());
    }

    @ExceptionHandler(RegraNegocioException.class)
    ProblemDetail tratarRegraNegocio(RegraNegocioException ex) {
        return problema(HttpStatus.UNPROCESSABLE_ENTITY, "Regra de negócio violada", ex.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ProblemDetail tratarConflitoDeIntegridade(DataIntegrityViolationException ex) {
        String causa = ex.getMostSpecificCause().getMessage();
        String detalhe = MENSAGENS_DE_CONFLITO.entrySet().stream()
                .filter(entrada -> causa != null && causa.contains(entrada.getKey()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(CONFLITO_GENERICO);
        return problema(HttpStatus.CONFLICT, "Conflito", detalhe);
    }

    @ExceptionHandler(FalhaBackupException.class)
    ProblemDetail tratarFalhaBackup(FalhaBackupException ex) {
        return problema(HttpStatus.INTERNAL_SERVER_ERROR, "Falha no backup", ex.getMessage());
    }

    @ExceptionHandler(XmlNfseInvalidoException.class)
    ProblemDetail tratarXmlInvalido(XmlNfseInvalidoException ex) {
        return problema(HttpStatus.BAD_REQUEST, "XML inválido", ex.getMessage());
    }

    @ExceptionHandler(ConflitoException.class)
    ProblemDetail tratarConflito(ConflitoException ex) {
        return problema(HttpStatus.CONFLICT, "Conflito", ex.getMessage());
    }

    private static ProblemDetail problema(HttpStatus status, String titulo, String detalhe) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(status, detalhe);
        problema.setTitle(titulo);
        return problema;
    }
}
