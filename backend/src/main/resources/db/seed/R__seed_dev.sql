-- Dados FICTÍCIOS para demonstração (perfil dev). Nunca colocar dados reais aqui.
-- Migration "repeatable" (R__): roda depois das versionadas e de novo sempre que este
-- arquivo mudar. Por isso todo INSERT precisa ser idempotente (ON CONFLICT DO NOTHING).
-- Os CNPJs são apenas válidos nos dígitos verificadores, não pertencem a empresas reais do sistema.
INSERT INTO cliente (nome, documento, email, produto, valor_mensal, dia_vencimento)
VALUES ('Barbearia Navalha de Ouro (fictícia)', '11222333000181', 'contato@navalha.example',   'SIR_BARBER', 199.90, 10),
       ('Pet Shop Amigo Fiel (fictícia)',       '11444777000161', 'contato@amigofiel.example', 'SIR_PET',    249.90, 15)
ON CONFLICT (documento) DO NOTHING;
