-- Dados FICTÍCIOS para demonstração (perfil dev). Nunca colocar dados reais aqui.
-- Não roda no perfil real. Migration repeatable: todo INSERT é idempotente.
-- Os CNPJs só são válidos nos dígitos verificadores.

-- Bancos de demonstração antigos usavam outro CNPJ na padaria. Troca no lugar, sem duplicar.
UPDATE cliente SET documento = '12345678000195'
WHERE nome = 'Padaria Pão Quente (fictícia)' AND documento <> '12345678000195';

INSERT INTO cliente (nome, documento, email, produto, valor_mensal, dia_vencimento)
VALUES ('Barbearia Navalha de Ouro (fictícia)', '11222333000181', 'contato@navalha.example',    'SIR_BARBER', 300.00, 10),
       ('Pet Shop Amigo Fiel (fictícia)',       '11444777000161', 'contato@amigofiel.example',  'SIR_PET',    200.00, 10),
       ('Padaria Pão Quente (fictícia)',        '12345678000195', 'contato@paoquente.example',  'SIR_FOOD',   180.00, 10)
ON CONFLICT (documento) DO UPDATE
    SET valor_mensal = EXCLUDED.valor_mensal,
        dia_vencimento = EXCLUDED.dia_vencimento;

-- Nota pendente acompanha o valor e o dia do cliente. Emitida ou paga fica como está.
UPDATE nota_fiscal AS nota
SET valor = cliente.valor_mensal,
    data_vencimento = nota.competencia + (cliente.dia_vencimento - 1)
FROM cliente
WHERE nota.cliente_id = cliente.id
  AND nota.status = 'PENDENTE'
  AND cliente.documento IN ('11222333000181', '11444777000161', '12345678000195');

-- Seis meses: os anteriores pagos, o corrente pendente.
INSERT INTO nota_fiscal (
    cliente_id, competencia, numero, valor, data_emissao, data_vencimento, data_pagamento, status)
SELECT cliente.id,
       mes.competencia::date,
       CASE WHEN mes.competencia::date = atual.dia THEN NULL
            ELSE 'DEMO-' || to_char(mes.competencia, 'YYYYMM') || '-' || right(cliente.documento, 4)
       END,
       cliente.valor_mensal,
       CASE WHEN mes.competencia::date = atual.dia THEN NULL ELSE mes.competencia::date + 4 END,
       mes.competencia::date + (cliente.dia_vencimento - 1),
       CASE WHEN mes.competencia::date = atual.dia THEN NULL ELSE mes.competencia::date + 9 END,
       CASE WHEN mes.competencia::date = atual.dia THEN 'PENDENTE' ELSE 'PAGA' END
FROM cliente
CROSS JOIN (
    SELECT date_trunc('month', now() AT TIME ZONE 'America/Sao_Paulo')::date AS dia
) AS atual
CROSS JOIN LATERAL generate_series(atual.dia - interval '5 months', atual.dia::timestamp, interval '1 month') AS mes(competencia)
WHERE cliente.documento IN ('11222333000181', '11444777000161', '12345678000195')
ON CONFLICT (cliente_id, competencia) WHERE status <> 'CANCELADA' DO NOTHING;

-- Servidor, domínio e ferramenta se repetem. O índice impede a segunda cópia no mesmo mês.
INSERT INTO despesa (descricao, categoria, valor, competencia, recorrente)
SELECT item.descricao, item.categoria, item.valor, mes.competencia::date, TRUE
FROM generate_series(
        date_trunc('month', now() AT TIME ZONE 'America/Sao_Paulo') - interval '5 months',
        date_trunc('month', now() AT TIME ZONE 'America/Sao_Paulo'),
        interval '1 month') AS mes(competencia)
CROSS JOIN (VALUES
    ('Servidor DigitalOcean (demo)', 'SERVIDOR', 150.00),
    ('Domínio (demo)',               'DOMINIO',   40.00),
    ('Ferramenta de e-mail (demo)',  'FERRAMENTAS', 30.00)
) AS item(descricao, categoria, valor)
ON CONFLICT (lower(descricao), categoria, competencia) WHERE recorrente DO NOTHING;

-- O Simples muda de valor e não se copia. Só nos meses que já fecharam.
INSERT INTO despesa (descricao, categoria, valor, competencia, recorrente)
SELECT 'Simples Nacional (demo)',
       'IMPOSTO',
       (70 + extract(month FROM mes.competencia))::numeric(12, 2),
       mes.competencia::date,
       FALSE
FROM generate_series(
        date_trunc('month', now() AT TIME ZONE 'America/Sao_Paulo') - interval '5 months',
        date_trunc('month', now() AT TIME ZONE 'America/Sao_Paulo') - interval '1 month',
        interval '1 month') AS mes(competencia)
WHERE NOT EXISTS (
    SELECT 1 FROM despesa existente
    WHERE existente.descricao = 'Simples Nacional (demo)'
      AND existente.categoria = 'IMPOSTO'
      AND existente.competencia = mes.competencia::date
);
