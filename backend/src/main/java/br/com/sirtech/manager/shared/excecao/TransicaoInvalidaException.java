package br.com.sirtech.manager.shared.excecao;

import br.com.sirtech.manager.nota.StatusNota;

/** Tentativa de mudar o status de uma nota por um caminho que o enum não permite. Vira HTTP 422. */
public class TransicaoInvalidaException extends RegraNegocioException {

    public TransicaoInvalidaException(StatusNota origem, StatusNota destino) {
        super("Não é possível mudar a nota de %s para %s".formatted(origem, destino));
    }
}
