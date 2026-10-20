package br.com.sirtech.manager.acesso;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import br.com.sirtech.manager.acesso.dto.Demonstracao;
import br.com.sirtech.manager.acesso.dto.EntrarRequest;
import br.com.sirtech.manager.acesso.dto.UsuarioLogado;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/acesso")
public class AcessoController {

    private final AcessoService service;
    private final Demonstracao demonstracao;

    public AcessoController(AcessoService service,
                            @Value("${sirtech.acesso.demonstracao:false}") boolean demonstracao,
                            @Value("${sirtech.acesso.inicial.email:}") String email,
                            @Value("${sirtech.acesso.inicial.senha:}") String senha) {
        this.service = service;
        this.demonstracao = demonstracao ? new Demonstracao(true, email, senha) : Demonstracao.desligada();
    }

    @PostMapping("/entrar")
    public UsuarioLogado entrar(@RequestBody @Valid EntrarRequest dados,
                                HttpServletRequest requisicao, HttpServletResponse resposta) {
        return service.entrar(dados, requisicao, resposta);
    }

    /**
     * A tela chama ao abrir. Responde quem está logado, ou 401.
     * Também força a criação do cookie {@code XSRF-TOKEN}, que o Angular precisa para o POST de entrar.
     */
    @GetMapping("/sessao")
    public ResponseEntity<UsuarioLogado> sessao(Authentication autenticacao, CsrfToken csrf) {
        csrf.getToken();
        return service.usuarioAtual(autenticacao)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }

    /** Diz à tela de entrar se pode mostrar o login de demonstração. No uso real, nunca. */
    @GetMapping("/demonstracao")
    public Demonstracao demonstracao() {
        return demonstracao;
    }

    @PostMapping("/sair")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void sair(HttpServletRequest requisicao) {
        service.sair(requisicao);
    }
}
