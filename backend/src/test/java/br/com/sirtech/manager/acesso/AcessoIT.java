package br.com.sirtech.manager.acesso;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import br.com.sirtech.manager.AbstractIntegrationTest;
import jakarta.servlet.http.Cookie;

/**
 * Prova o login de ponta a ponta, com os filtros de segurança ligados e o PostgreSQL real.
 * Monta o próprio MockMvc: aqui nenhuma requisição sai logada por padrão.
 *
 * <p>O token CSRF segue o caminho da tela: lê o cookie {@code XSRF-TOKEN} e devolve no cabeçalho
 * {@code X-XSRF-TOKEN}. Não usa o atalho {@code csrf()} do spring-security-test, porque ele troca
 * para sempre o repositório de token do filtro (cookie vira sessão). O contexto próprio
 * ({@link TestPropertySource}) isola esta classe dos outros testes que usam esse atalho.
 */
@TestPropertySource(properties = "sirtech.teste.contexto=acesso")
class AcessoIT extends AbstractIntegrationTest {

    private static final String SENHA = "senha-forte-123";

    @Autowired
    private WebApplicationContext contexto;

    @Autowired
    private UsuarioRepository usuarios;

    @Autowired
    private PasswordEncoder senhas;

    @Autowired
    private LimiteTentativas limite;

    private MockMvc mockMvc;

    @BeforeEach
    void preparar() {
        mockMvc = MockMvcBuilders.webAppContextSetup(contexto).apply(springSecurity()).build();
        usuarios.save(new Usuario("Dona da Sir Tech", "dona@sirtech.example", senhas.encode(SENHA), LocalDateTime.now()));
    }

    @AfterEach
    void limpar() {
        usuarios.deleteAll();
        limite.limpar("dona@sirtech.example");
    }

    @Test
    void apiSemLoginResponde401EmProblemDetail() throws Exception {
        mockMvc.perform(get("/api/clientes"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.detail").value("Entre no sistema para continuar"));
    }

    @Test
    void sessaoSemLoginResponde401EEntregaOTokenCsrf() throws Exception {
        mockMvc.perform(get("/api/acesso/sessao"))
                .andExpect(status().isUnauthorized())
                .andExpect(cookie().exists("XSRF-TOKEN"));
    }

    @Test
    void entrarSemTokenCsrfEhRecusado() throws Exception {
        mockMvc.perform(post("/api/acesso/entrar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(credenciais("dona@sirtech.example", SENHA)))
                .andExpect(status().isForbidden());
    }

    @Test
    void entrarAbreSessaoQueLiberaAApiESairFechaDeNovo() throws Exception {
        MockHttpSession sessao = (MockHttpSession) entrar("DONA@sirtech.example ", SENHA)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Dona da Sir Tech"))
                .andExpect(jsonPath("$.email").value("dona@sirtech.example"))
                .andExpect(jsonPath("$.senhaHash").doesNotExist())
                .andReturn().getRequest().getSession();

        mockMvc.perform(get("/api/clientes").session(sessao)).andExpect(status().isOk());
        mockMvc.perform(get("/api/acesso/sessao").session(sessao))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Dona da Sir Tech"));

        mockMvc.perform(comCsrf(post("/api/acesso/sair")).session(sessao))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/clientes").session(sessao)).andExpect(status().isUnauthorized());
    }

    @Test
    void senhaErradaResponde401SemDizerSeOEmailExiste() throws Exception {
        entrar("dona@sirtech.example", "errada-123")
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("E-mail ou senha incorretos"));
        entrar("ninguem@sirtech.example", "errada-123")
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("E-mail ou senha incorretos"));
    }

    @Test
    void cincoSenhasErradasBloqueiamAteASenhaCerta() throws Exception {
        for (int i = 0; i < LimiteTentativas.MAXIMO_DE_FALHAS; i++) {
            entrar("dona@sirtech.example", "errada-123").andExpect(status().isUnauthorized());
        }

        entrar("dona@sirtech.example", SENHA)
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.status").value(429));
    }

    @Test
    void senhaFicaGuardadaComoHashBcrypt() {
        Usuario salvo = usuarios.findByEmail("dona@sirtech.example").orElseThrow();

        assertThat(salvo.getSenhaHash()).startsWith("$2").doesNotContain(SENHA);
        assertThat(senhas.matches(SENHA, salvo.getSenhaHash())).isTrue();
    }

    @Test
    void foraDaDemonstracaoNaoEntregaLoginDeExemplo() throws Exception {
        mockMvc.perform(get("/api/acesso/demonstracao"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ativa").value(false))
                .andExpect(jsonPath("$.senha").doesNotExist());
    }

    @Test
    void swaggerContinuaAbertoParaLeitura() throws Exception {
        mockMvc.perform(get("/v3/api-docs")).andExpect(status().isOk());
    }

    private ResultActions entrar(String email, String senha) throws Exception {
        return mockMvc.perform(comCsrf(post("/api/acesso/entrar"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(credenciais(email, senha)));
    }

    /** Faz o que o Angular faz: pega o token do cookie e devolve no cabeçalho. */
    private MockHttpServletRequestBuilder comCsrf(MockHttpServletRequestBuilder requisicao) throws Exception {
        Cookie cookie = mockMvc.perform(get("/api/acesso/sessao")).andReturn().getResponse().getCookie("XSRF-TOKEN");
        assertThat(cookie).isNotNull();
        return requisicao.cookie(cookie).header("X-XSRF-TOKEN", cookie.getValue());
    }

    private static String credenciais(String email, String senha) {
        return """
                {"email":"%s","senha":"%s"}
                """.formatted(email.strip(), senha);
    }
}
