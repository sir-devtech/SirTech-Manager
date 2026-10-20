package br.com.sirtech.manager.despesa;

import java.math.BigDecimal;
import java.time.YearMonth;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Gasto de um mês. Se for recorrente, a geração mensal copia para o mês seguinte. */
@Entity
@Table(name = "despesa")
public class Despesa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CategoriaDespesa categoria;

    @Column(nullable = false)
    private BigDecimal valor;

    @Column(nullable = false)
    private YearMonth competencia;

    @Column(nullable = false)
    private boolean recorrente;

    protected Despesa() {
    }

    public Despesa(String descricao, CategoriaDespesa categoria, BigDecimal valor,
                   YearMonth competencia, boolean recorrente) {
        atualizar(descricao, categoria, valor, competencia, recorrente);
    }

    public void atualizar(String descricao, CategoriaDespesa categoria, BigDecimal valor,
                          YearMonth competencia, boolean recorrente) {
        this.descricao = descricao;
        this.categoria = categoria;
        this.valor = valor;
        this.competencia = competencia;
        this.recorrente = recorrente;
    }

    public Long getId() {
        return id;
    }

    public String getDescricao() {
        return descricao;
    }

    public CategoriaDespesa getCategoria() {
        return categoria;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public YearMonth getCompetencia() {
        return competencia;
    }

    public boolean isRecorrente() {
        return recorrente;
    }
}
