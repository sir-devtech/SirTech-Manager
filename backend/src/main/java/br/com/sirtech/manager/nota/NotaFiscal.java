package br.com.sirtech.manager.nota;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;

import br.com.sirtech.manager.cliente.Cliente;
import br.com.sirtech.manager.shared.excecao.RegraNegocioException;
import br.com.sirtech.manager.shared.excecao.TransicaoInvalidaException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Nota fiscal da Sir Tech. A emissão de verdade acontece no site da prefeitura;
 * aqui ficam valor, vencimento, o XML importado e a passagem de status.
 */
@Entity
@Table(name = "nota_fiscal")
public class NotaFiscal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @Column(nullable = false)
    private YearMonth competencia;

    private String numero;

    @Column(nullable = false)
    private BigDecimal valor;

    @Column(name = "data_emissao")
    private LocalDate dataEmissao;

    @Column(name = "data_vencimento", nullable = false)
    private LocalDate dataVencimento;

    @Column(name = "data_pagamento")
    private LocalDate dataPagamento;

    /** XML completo da NFS-e, só no banco local. Não vai para o Git. */
    @Column(name = "xml_original", columnDefinition = "text")
    private String xmlOriginal;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusNota status;

    protected NotaFiscal() {
    }

    public NotaFiscal(Cliente cliente, YearMonth competencia, BigDecimal valor, LocalDate dataVencimento) {
        this.cliente = cliente;
        this.competencia = competencia;
        this.valor = valor;
        this.dataVencimento = dataVencimento;
        this.status = StatusNota.PENDENTE;
    }

    public void emitir(String numero, LocalDate dataEmissao) {
        if (numero == null || numero.isBlank() || dataEmissao == null) {
            throw new RegraNegocioException("Emitir exige número e data de emissão");
        }
        exigirTransicao(StatusNota.EMITIDA);
        this.numero = numero.trim();
        this.dataEmissao = dataEmissao;
        this.status = StatusNota.EMITIDA;
    }

    public void pagar(LocalDate dataPagamento) {
        if (dataPagamento == null) {
            throw new RegraNegocioException("Pagar exige data de pagamento");
        }
        exigirTransicao(StatusNota.PAGA);
        if (dataEmissao != null && dataPagamento.isBefore(dataEmissao)) {
            throw new RegraNegocioException("A data de pagamento não pode ser anterior à emissão");
        }
        this.dataPagamento = dataPagamento;
        this.status = StatusNota.PAGA;
    }

    public void cancelar() {
        exigirTransicao(StatusNota.CANCELADA);
        this.status = StatusNota.CANCELADA;
    }

    /** A previsão muda junto com o cliente enquanto a nota ainda não foi emitida. */
    public void ajustarValor(BigDecimal novoValor) {
        if (status != StatusNota.PENDENTE) {
            throw new RegraNegocioException("Só é possível ajustar o valor de uma nota pendente");
        }
        if (novoValor == null || novoValor.signum() <= 0) {
            throw new RegraNegocioException("O valor da nota precisa ser maior que zero");
        }
        this.valor = novoValor;
    }

    public void ajustarVencimento(LocalDate novaData) {
        if (status != StatusNota.PENDENTE) {
            throw new RegraNegocioException("Só é possível ajustar o vencimento de uma nota pendente");
        }
        if (novaData == null) {
            throw new RegraNegocioException("O vencimento é obrigatório");
        }
        this.dataVencimento = novaData;
    }

    public void guardarXml(String xml) {
        this.xmlOriginal = xml;
    }

    /** Vencida é calculada: emitida e com vencimento antes de hoje. Não fica gravada. */
    public boolean isVencida(LocalDate hoje) {
        return status == StatusNota.EMITIDA && dataVencimento.isBefore(hoje);
    }

    private void exigirTransicao(StatusNota destino) {
        if (!status.podeTransicionarPara(destino)) {
            throw new TransicaoInvalidaException(status, destino);
        }
    }

    public Long getId() {
        return id;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public YearMonth getCompetencia() {
        return competencia;
    }

    public String getNumero() {
        return numero;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public LocalDate getDataEmissao() {
        return dataEmissao;
    }

    public LocalDate getDataVencimento() {
        return dataVencimento;
    }

    public LocalDate getDataPagamento() {
        return dataPagamento;
    }

    public StatusNota getStatus() {
        return status;
    }

    public String getXmlOriginal() {
        return xmlOriginal;
    }
}
