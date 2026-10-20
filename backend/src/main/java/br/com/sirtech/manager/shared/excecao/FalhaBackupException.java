package br.com.sirtech.manager.shared.excecao;

/** O pg_dump não conseguiu gravar o arquivo. Vira HTTP 500. */
public class FalhaBackupException extends RuntimeException {

    public FalhaBackupException(String mensagem) {
        super(mensagem);
    }
}
