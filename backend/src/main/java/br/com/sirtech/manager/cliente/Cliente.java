package br.com.sirtech.manager.cliente;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Cliente que assina um produto da Sir Tech.
 * Não há setters públicos: o estado só muda por métodos com significado de negócio
 * ({@link #atualizar}, {@link #ativar}, {@link #desativar}).
 */
@Entity
@Table(name = "cliente")
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false, unique = true)
    private String documento;

    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Produto produto;

    @Column(name = "valor_mensal", nullable = false)
    private BigDecimal valorMensal;

    @Column(name = "dia_vencimento", nullable = false)
    private Integer diaVencimento;

    @Column(nullable = false)
    private boolean ativo = true;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    /** Exigido pelo JPA. */
    protected Cliente() {
    }

    public Cliente(String nome, String documento, String email, Produto produto,
                   BigDecimal valorMensal, Integer diaVencimento, LocalDateTime criadoEm) {
        atualizar(nome, documento, email, produto, valorMensal, diaVencimento);
        this.criadoEm = criadoEm;
    }

    public void atualizar(String nome, String documento, String email, Produto produto,
                          BigDecimal valorMensal, Integer diaVencimento) {
        this.nome = nome;
        this.documento = documento;
        this.email = email;
        this.produto = produto;
        this.valorMensal = valorMensal;
        this.diaVencimento = diaVencimento;
    }

    public void ativar() {
        this.ativo = true;
    }

    public void desativar() {
        this.ativo = false;
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getDocumento() {
        return documento;
    }

    public String getEmail() {
        return email;
    }

    public Produto getProduto() {
        return produto;
    }

    public BigDecimal getValorMensal() {
        return valorMensal;
    }

    public Integer getDiaVencimento() {
        return diaVencimento;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }
}
