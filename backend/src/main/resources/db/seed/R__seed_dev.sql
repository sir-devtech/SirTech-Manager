-- Dados FICTÍCIOS para demonstração (perfil dev). Nunca colocar dados reais aqui.
-- Migration "repeatable" (R__): roda depois das versionadas e de novo sempre que este
-- arquivo mudar. Por isso todo INSERT precisa ser idempotente (ON CONFLICT DO NOTHING).
-- Os CNPJs são apenas válidos nos dígitos verificadores, não pertencem a empresas reais do sistema.
INSERT INTO cliente (nome, documento, email, produto, valor_mensal, dia_vencimento)
VALUES ('Barbearia Navalha de Ouro (fictícia)', '11222333000181', 'contato@navalha.example',   'SIR_BARBER', 300.00, 10),
       ('Pet Shop Amigo Fiel (fictícia)',       '11444777000161', 'contato@amigofiel.example', 'SIR_PET',    200.00, 10)
ON CONFLICT (documento) DO UPDATE
    SET valor_mensal = EXCLUDED.valor_mensal,
        dia_vencimento = EXCLUDED.dia_vencimento;

-- Nota pendente é a previsão do mês: acompanha o valor e o dia do cliente.
-- Emitida ou paga fica como está.
UPDATE nota_fiscal AS nota
SET valor = cliente.valor_mensal,
    data_vencimento = nota.competencia + (cliente.dia_vencimento - 1)
FROM cliente
WHERE nota.cliente_id = cliente.id
  AND nota.status = 'PENDENTE'
  AND cliente.documento IN ('11222333000181', '11444777000161');

-- Servidor pago no cartão todo mês. Recorrente: a geração do mês seguinte copia.
INSERT INTO despesa (descricao, categoria, valor, competencia, recorrente)
VALUES ('Servidor DigitalOcean (demo)', 'SERVIDOR', 150.00,
        date_trunc('month', now() AT TIME ZONE 'America/Sao_Paulo')::date, TRUE)
ON CONFLICT DO NOTHING;
