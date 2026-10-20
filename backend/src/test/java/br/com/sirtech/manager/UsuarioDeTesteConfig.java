package br.com.sirtech.manager;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.MockMvcBuilderCustomizer;
import org.springframework.context.annotation.Bean;

/**
 * Nos testes de integração das funcionalidades, toda requisição já sai logada e com token CSRF.
 * Assim cada teste trata do assunto dele. Quem prova o login de verdade é o {@code AcessoIT},
 * que monta o próprio MockMvc sem este atalho.
 */
@TestConfiguration(proxyBeanMethods = false)
public class UsuarioDeTesteConfig {

    @Bean
    MockMvcBuilderCustomizer requisicoesLogadas() {
        return builder -> builder.defaultRequest(get("/").with(user("teste@sirtech.example")).with(csrf()));
    }
}
