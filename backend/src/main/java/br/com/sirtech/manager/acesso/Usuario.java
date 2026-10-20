package br.com.sirtech.manager.acesso;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Quem pode entrar no sistema. A senha em texto nunca chega aqui: a entidade só conhece o hash.
 */
@Entity
@Table(name = "usuario")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "senha_hash", nullable = false)
    private String senhaHash;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    /** Exigido pelo JPA. */
    protected Usuario() {
    }

    public Usuario(String nome, String email, String senhaHash, LocalDateTime criadoEm) {
        this.nome = nome;
        this.email = normalizarEmail(email);
        this.senhaHash = senhaHash;
        this.criadoEm = criadoEm;
    }

    /** O e-mail é guardado e comparado sempre em minúsculas, sem espaços nas pontas. */
    public static String normalizarEmail(String email) {
        return email == null ? "" : email.strip().toLowerCase();
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getEmail() {
        return email;
    }

    public String getSenhaHash() {
        return senhaHash;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }
}
