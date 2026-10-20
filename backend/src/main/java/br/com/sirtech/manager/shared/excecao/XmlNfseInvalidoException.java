package br.com.sirtech.manager.shared.excecao;

/** O arquivo não é uma NFS-e legível. Vira HTTP 400. */
public class XmlNfseInvalidoException extends RuntimeException {

    public XmlNfseInvalidoException(String mensagem) {
        super(mensagem);
    }
}
