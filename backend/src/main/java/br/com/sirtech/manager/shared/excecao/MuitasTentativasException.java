package br.com.sirtech.manager.shared.excecao;

/** Login bloqueado por excesso de senhas erradas. Vira HTTP 429. */
public class MuitasTentativasException extends RuntimeException {

    public MuitasTentativasException(String mensagem) {
        super(mensagem);
    }
}
