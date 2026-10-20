-- Quem pode entrar no sistema. A senha nunca é guardada: só o hash BCrypt dela.
CREATE TABLE usuario (
    id         BIGSERIAL    PRIMARY KEY,
    nome       VARCHAR(100) NOT NULL,
    email      VARCHAR(150) NOT NULL,
    senha_hash VARCHAR(100) NOT NULL,
    criado_em  TIMESTAMP    NOT NULL DEFAULT now(),

    CONSTRAINT uk_usuario_email UNIQUE (email),
    CONSTRAINT ck_usuario_email_minusculo CHECK (email = lower(email))
);
