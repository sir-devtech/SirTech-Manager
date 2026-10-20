package br.com.sirtech.manager.acesso;

import java.util.Optional;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.sirtech.manager.acesso.dto.EntrarRequest;
import br.com.sirtech.manager.acesso.dto.UsuarioLogado;
import br.com.sirtech.manager.shared.excecao.CredenciaisInvalidasException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Service
public class AcessoService {

    private final AuthenticationManager autenticacao;
    private final SecurityContextRepository contextos;
    private final LimiteTentativas limite;
    private final UsuarioRepository usuarios;

    public AcessoService(AuthenticationManager autenticacao, SecurityContextRepository contextos,
                         LimiteTentativas limite, UsuarioRepository usuarios) {
        this.autenticacao = autenticacao;
        this.contextos = contextos;
        this.limite = limite;
        this.usuarios = usuarios;
    }

    /**
     * Confere e-mail e senha, troca o id da sessão e grava quem entrou nela.
     * Trocar o id impede que alguém que plantou um id de sessão antes do login herde o acesso
     * (ataque de fixação de sessão).
     */
    @Transactional(readOnly = true)
    public UsuarioLogado entrar(EntrarRequest dados, HttpServletRequest requisicao, HttpServletResponse resposta) {
        String email = Usuario.normalizarEmail(dados.email());
        limite.verificar(email);

        Authentication autenticado;
        try {
            autenticado = autenticacao.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(email, dados.senha()));
        } catch (AuthenticationException erro) {
            limite.registrarFalha(email);
            throw new CredenciaisInvalidasException();
        }
        limite.limpar(email);

        if (requisicao.getSession(false) != null) {
            requisicao.changeSessionId();
        }
        SecurityContext contexto = SecurityContextHolder.createEmptyContext();
        contexto.setAuthentication(autenticado);
        SecurityContextHolder.setContext(contexto);
        contextos.saveContext(contexto, requisicao, resposta);

        return usuarioAtual(autenticado).orElseThrow(CredenciaisInvalidasException::new);
    }

    @Transactional(readOnly = true)
    public Optional<UsuarioLogado> usuarioAtual(Authentication autenticacaoAtual) {
        if (autenticacaoAtual == null || !autenticacaoAtual.isAuthenticated()
                || "anonymousUser".equals(autenticacaoAtual.getPrincipal())) {
            return Optional.empty();
        }
        return usuarios.findByEmail(autenticacaoAtual.getName()).map(UsuarioLogado::de);
    }

    public void sair(HttpServletRequest requisicao) {
        HttpSession sessao = requisicao.getSession(false);
        if (sessao != null) {
            sessao.invalidate();
        }
        SecurityContextHolder.clearContext();
    }
}
