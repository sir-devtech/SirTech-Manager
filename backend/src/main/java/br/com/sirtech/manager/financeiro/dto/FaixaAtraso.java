package br.com.sirtech.manager.financeiro.dto;

import java.math.BigDecimal;

/**
 * Notas emitidas e ainda não pagas, olhando hoje. Não depende do período escolhido.
 * A vencer inclui o que vence hoje. As outras três são atraso.
 */
public record FaixaAtraso(
        int aVencer,
        BigDecimal valorAVencer,
        int ate30,
        BigDecimal valorAte30,
        int de31a60,
        BigDecimal valorDe31a60,
        int acimaDe60,
        BigDecimal valorAcimaDe60) {
}
