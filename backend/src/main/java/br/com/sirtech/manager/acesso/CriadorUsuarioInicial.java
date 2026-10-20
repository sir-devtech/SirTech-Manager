package br.com.sirtech.manager.acesso;

import java.time.Clock;
import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Cria o primeiro usuário quando a tabela está vazia. Os dados vêm de variáveis de ambiente
 * ({@code SIRTECH_ADMIN_*}), nunca do código: assim a senha real não entra no Git.
 * Com usuário já cadastrado, não faz nada.
 */
@Component
public class CriadorUsuarioInicial implements ApplicationRunner {

    static final int TAMANHO_MINIMO_SENHA = 8;
    private static final Logger LOG = LoggerFactory.getLogger(CriadorUsuarioInicial.class);

    private final UsuarioRepository usuarios;
    private final PasswordEncoder senhas;
    private final Clock clock;
    private final String nome;
    private final String email;
    private final String senha;

    public CriadorUsuarioInicial(UsuarioRepository usuarios, PasswordEncoder senhas, Clock clock,
                                 @Value("${sirtech.acesso.inicial.nome:}") String nome,
                                 @Value("${sirtech.acesso.inicial.email:}") String email,
                                 @Value("${sirtech.acesso.inicial.senha:}") String senha) {
        this.usuarios = usuarios;
        this.senhas = senhas;
        this.clock = clock;
        this.nome = nome;
        this.email = email;
        this.senha = senha;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (usuarios.count() > 0) {
            return;
        }
        if (email.isBlank() || senha.isBlank()) {
            LOG.warn("Nenhum usuário cadastrado. Defina SIRTECH_ADMIN_EMAIL e SIRTECH_ADMIN_SENHA para criar o primeiro.");
            return;
        }
        if (senha.length() < TAMANHO_MINIMO_SENHA) {
            throw new IllegalStateException(
                    "SIRTECH_ADMIN_SENHA precisa ter pelo menos %d caracteres".formatted(TAMANHO_MINIMO_SENHA));
        }
        String nomeExibido = nome.isBlank() ? "Administrador" : nome;
        usuarios.save(new Usuario(nomeExibido, email, senhas.encode(senha), LocalDateTime.now(clock)));
        LOG.info("Primeiro usuário criado: {}", Usuario.normalizarEmail(email));
    }
}
