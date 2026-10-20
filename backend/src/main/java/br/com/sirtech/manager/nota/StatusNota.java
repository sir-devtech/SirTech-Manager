package br.com.sirtech.manager.nota;

/**
 * Estados da nota e as únicas passagens permitidas.
 * PAGA e CANCELADA não sobrescrevem o método: dali não se sai.
 */
public enum StatusNota {
    PENDENTE {
        @Override
        public boolean podeTransicionarPara(StatusNota destino) {
            return destino == EMITIDA || destino == CANCELADA;
        }
    },
    EMITIDA {
        @Override
        public boolean podeTransicionarPara(StatusNota destino) {
            return destino == PAGA || destino == CANCELADA;
        }
    },
    PAGA,
    CANCELADA;

    public boolean podeTransicionarPara(StatusNota destino) {
        return false;
    }
}
