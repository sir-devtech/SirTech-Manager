CREATE TABLE cliente (
    id             BIGSERIAL      PRIMARY KEY,
    nome           VARCHAR(150)   NOT NULL,
    documento      VARCHAR(14)    NOT NULL,
    email          VARCHAR(150),
    produto        VARCHAR(20)    NOT NULL,
    valor_mensal   NUMERIC(12, 2) NOT NULL,
    dia_vencimento INTEGER        NOT NULL,
    ativo          BOOLEAN        NOT NULL DEFAULT TRUE,
    criado_em      TIMESTAMP      NOT NULL DEFAULT now(),

    CONSTRAINT uk_cliente_documento UNIQUE (documento),
    CONSTRAINT ck_cliente_documento CHECK (documento ~ '^([0-9]{11}|[0-9]{14})$'),
    CONSTRAINT ck_cliente_produto CHECK (produto IN ('SIR_BARBER', 'SIR_PET', 'SIR_FOOD', 'OUTRO')),
    CONSTRAINT ck_cliente_valor_mensal CHECK (valor_mensal > 0),
    CONSTRAINT ck_cliente_dia_vencimento CHECK (dia_vencimento BETWEEN 1 AND 28)
);
