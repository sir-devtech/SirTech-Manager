# ADR 0009 — Login com sessão no servidor, não com JWT

**Status:** aceito · **Fase:** login (revoga o "sem login" do escopo original)

## Contexto
O escopo deixava login de fora: o sistema roda num computador e tem uma usuária. Mas a API ficava aberta para qualquer programa da máquina, ou da rede, quando as portas ainda escutavam em todas as interfaces, e um sistema com dados financeiros e documentos de clientes não deveria responder a qualquer um.

A tela (Angular) e a API (Spring) são servidas pela **mesma origem**: o nginx entrega a tela e repassa `/api` para a API.

## Decisão
- **Sessão no servidor**, identificada por um cookie `HttpOnly` e `SameSite=Strict`, que expira após 8 horas parado. O JavaScript da página não lê esse cookie.
- **Senha em hash BCrypt** com custo 12. A tabela `usuario` guarda só o hash.
- **CSRF ligado no modo SPA**: o Angular lê o cookie `XSRF-TOKEN` e devolve no cabeçalho `X-XSRF-TOKEN`.
- **Bloqueio após 5 senhas erradas seguidas**, por 15 minutos, em memória.
- **Troca do id da sessão no login**, contra fixação de sessão.
- **401, 403 e 429 no formato ProblemDetail**, pelo mesmo `GlobalExceptionHandler` do resto da API.
- **Primeiro usuário criado por variável de ambiente**, só com a tabela vazia. O login de demonstração existe só no perfil `dev`.

## Alternativas consideradas
- **JWT no `localStorage`**: comum em tutorial, mas o token fica ao alcance de qualquer script injetado (XSS), e logout de verdade exige lista de tokens revogados. Faz sentido com várias APIs ou app móvel; aqui é uma origem só.
- **JWT em cookie HttpOnly**: resolve o XSS, mas volta a precisar de CSRF e não ganha nada sobre a sessão, que o Spring já sabe gerenciar.
- **OAuth/OpenID com provedor externo** (Google, Keycloak): pesado para uma pessoa, e o sistema é offline.
- **Continuar sem login**: deixaria dados financeiros e documentos de clientes acessíveis a qualquer programa que alcançasse a API.

## Consequências
- A API inteira passa a exigir login, menos entrar, consultar a sessão e a dica da demonstração. O Swagger continua aberto para leitura.
- Os testes de controller desligam os filtros (testam validação e formato de erro); os de integração entram como um usuário de teste; o `AcessoIT` prova o login de ponta a ponta, com CSRF e PostgreSQL real.
- O bloqueio em memória zera quando a API reinicia. Com um processo e uma pessoa, é aceitável; com várias instâncias, iria para o banco ou para um cache compartilhado.
- O cookie não é `Secure` porque roda em `http://localhost`. Fora da máquina, com HTTPS, passa a ser obrigatório (ver [segurança](../seguranca.md)).
