CREATE TABLE despesa (
    id           BIGSERIAL      PRIMARY KEY,
    descricao    VARCHAR(150)   NOT NULL,
    categoria    VARCHAR(20)    NOT NULL,
    valor        NUMERIC(12, 2) NOT NULL,
    competencia  DATE           NOT NULL,
    recorrente   BOOLEAN        NOT NULL DEFAULT FALSE,

    CONSTRAINT ck_despesa_valor CHECK (valor > 0),
    CONSTRAINT ck_despesa_categoria CHECK (
        categoria IN ('CONTADOR', 'SERVIDOR', 'DOMINIO', 'FERRAMENTAS', 'IMPOSTO', 'OUTROS')
    )
);

-- A mesma despesa recorrente não entra duas vezes no mês. Maiúsculas não criam outra.
CREATE UNIQUE INDEX uk_despesa_recorrente
    ON despesa (lower(descricao), categoria, competencia)
    WHERE recorrente;
