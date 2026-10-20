package br.com.sirtech.manager.shared.excecao;

public class RecursoNaoEncontradoException extends RuntimeException {

    public RecursoNaoEncontradoException(String recurso, Long id) {
        super("%s com id %d não encontrado(a)".formatted(recurso, id));
    }
}
