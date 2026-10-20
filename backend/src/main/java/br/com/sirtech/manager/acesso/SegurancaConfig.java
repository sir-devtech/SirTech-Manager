package br.com.sirtech.manager.acesso;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.servlet.HandlerExceptionResolver;

/**
 * Regras de acesso da API.
 *
 * <ul>
 *   <li>Sessão no servidor, identificada por um cookie HttpOnly: o JavaScript da tela não consegue
 *       ler o cookie, então um script injetado não rouba a sessão.</li>
 *   <li>CSRF ligado no modo SPA: o Angular lê o cookie {@code XSRF-TOKEN} e devolve o valor no
 *       cabeçalho {@code X-XSRF-TOKEN}. Outro site não consegue ler esse cookie, então não forja
 *       um POST em nome de quem está logado.</li>
 *   <li>Tudo em {@code /api} exige login, menos entrar, consultar a sessão e a dica da demonstração.</li>
 *   <li>401 e 403 saem no mesmo formato ProblemDetail do resto da API: o filtro entrega a exceção
 *       ao {@code GlobalExceptionHandler}.</li>
 * </ul>
 */
@Configuration
public class SegurancaConfig {

    @Bean
    SecurityFilterChain filtroDeSeguranca(
            HttpSecurity http,
            SecurityContextRepository contextos,
            @Qualifier("handlerExceptionResolver") HandlerExceptionResolver erros) throws Exception {

        http
                .authorizeHttpRequests(regras -> regras
                        .requestMatchers(HttpMethod.POST, "/api/acesso/entrar").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/acesso/sessao", "/api/acesso/demonstracao").permitAll()
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/error").permitAll()
                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().denyAll())
                .csrf(csrf -> csrf.spa())
                .securityContext(contexto -> contexto.securityContextRepository(contextos))
                .exceptionHandling(excecoes -> excecoes
                        .authenticationEntryPoint((req, res, ex) -> erros.resolveException(req, res, null, ex))
                        .accessDeniedHandler((req, res, ex) -> erros.resolveException(req, res, null, ex)))
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                .logout(saida -> saida.disable());
        return http.build();
    }

    /** Guarda quem entrou na sessão HTTP. O login manual do {@link AcessoService} usa o mesmo lugar. */
    @Bean
    SecurityContextRepository repositorioDeContexto() {
        return new HttpSessionSecurityContextRepository();
    }

    /** BCrypt com custo 12: lento de propósito, para adivinhar senha por força bruta ficar caro. */
    @Bean
    PasswordEncoder codificadorDeSenha() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    AuthenticationManager gerenciadorDeAutenticacao(UsuarioDetailsService usuarios, PasswordEncoder senhas) {
        DaoAuthenticationProvider provedor = new DaoAuthenticationProvider(usuarios);
        provedor.setPasswordEncoder(senhas);
        return new ProviderManager(provedor);
    }
}
