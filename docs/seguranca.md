# Segurança

Os controles que valem hoje e o que mudaria se o sistema saísse da máquina local. A decisão sobre o login está no [ADR 0009](adr/0009-login-com-sessao.md).

## Login e sessão

| Controle | Como |
|---|---|
| Autenticação | E-mail e senha em `POST /api/acesso/entrar`. A mensagem de erro é a mesma para e-mail inexistente e senha errada. |
| Senha | Guardada só como hash **BCrypt** (custo 12). A tabela `usuario` não tem a senha. |
| Sessão | Mantida no servidor, identificada por cookie `HttpOnly` e `SameSite=Strict`; expira após 8 horas sem uso. O id é trocado no login (contra fixação de sessão). |
| CSRF | Modo SPA do Spring Security: a tela lê o cookie `XSRF-TOKEN` e devolve o valor no cabeçalho `X-XSRF-TOKEN`. |
| Força bruta | 5 senhas erradas seguidas bloqueiam o e-mail por 15 minutos (HTTP 429). |
| Autorização | Tudo em `/api` exige sessão, exceto entrar, consultar a sessão e a dica de demonstração. O Swagger fica aberto para leitura. |
| Erros | 401, 403 e 429 saem no mesmo formato `ProblemDetail` do resto da API. |
| Primeiro usuário | Criado a partir de variáveis de ambiente, só quando a tabela está vazia. O login de demonstração existe apenas no perfil `dev`. |

A tela tem uma guarda de rotas: sem sessão, qualquer página leva para a tela de entrar, e um 401 da API faz o mesmo.

## Dados e ambiente

- Dados reais e segredos ficam fora do Git: `.env`, backups e XML de clientes estão no `.gitignore`. Os dados de demonstração são fictícios.
- As portas do banco, da API e da tela escutam só em `127.0.0.1`: outras máquinas da mesma rede não alcançam o sistema.
- O uso real exige senha própria para o banco e para o primeiro usuário; sem elas, o Compose não sobe.
- O backup chama o `pg_dump` passando a senha por variável de ambiente, não pela linha de comando.
- O leitor de XML recusa DTD e entidades externas (proteção contra XXE).

## Dependências

O CI roda o `npm audit` e o **OSV-Scanner** em `pom.xml` e nos `package-lock.json` a cada push e toda segunda-feira, porque uma falha em biblioteca pode ser descoberta sem nenhum commit no projeto.

Na primeira execução, a verificação encontrou falhas reais e elas foram corrigidas:

| Onde | Falha | Correção |
|---|---|---|
| API | Tomcat 11.0.24 (3 críticas) e Jackson 2.21.5 e 3.1.5, trazidos pelo Spring Boot 4.1.1 | Versões corrigidas fixadas por propriedades da BOM no `pom.xml` (Tomcat 11.0.27, Jackson 2.21.7 e 3.1.7), com comentário de quando remover |
| Tela | `vite` 8.3.0 a 8.3.2 (alta, só no servidor de desenvolvimento), preso pelo `@angular/build` | `overrides` no `package.json` fixando o `vite` 8.3.4 |

## Se o sistema sair da máquina local

Hoje ele roda num computador, com uma pessoa usando. Estes pontos passariam a ser necessários:

- HTTPS, com o cookie da sessão marcado como `Secure`, e cabeçalhos de segurança (HSTS, CSP).
- Bloqueio de tentativas compartilhado entre instâncias (cache ou banco) e limite por endereço de origem.
- Senha própria até para a demonstração.
- Endpoint de saúde e métricas, para perceber queda sem abrir o terminal.
- Log sem documento de cliente, e registro de quem alterou o quê.
- Backup cifrado, fora da máquina e com restauração testada.
- Canal para o titular dos dados pedir cópia e exclusão (LGPD).
- Migrações de banco com caminho de volta para mudanças que apagam dados.
