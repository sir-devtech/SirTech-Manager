CREATE TABLE nota_fiscal (
    id               BIGSERIAL      PRIMARY KEY,
    cliente_id       BIGINT         NOT NULL REFERENCES cliente (id),
    competencia      DATE           NOT NULL,
    numero           VARCHAR(40),
    valor            NUMERIC(12, 2) NOT NULL,
    data_emissao     DATE,
    data_vencimento  DATE           NOT NULL,
    data_pagamento   DATE,
    status           VARCHAR(20)    NOT NULL,

    CONSTRAINT ck_nota_valor CHECK (valor > 0),
    CONSTRAINT ck_nota_status CHECK (status IN ('PENDENTE', 'EMITIDA', 'PAGA', 'CANCELADA'))
);

-- Uma nota ativa por cliente e mês. Cancelada sai do índice e libera o mês de novo.
CREATE UNIQUE INDEX uk_nota_cliente_competencia
    ON nota_fiscal (cliente_id, competencia)
    WHERE status <> 'CANCELADA';

CREATE INDEX idx_nota_competencia_status
    ON nota_fiscal (competencia, status);
