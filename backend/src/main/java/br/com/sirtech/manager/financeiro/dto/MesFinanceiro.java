package br.com.sirtech.manager.financeiro.dto;

import java.math.BigDecimal;
import java.time.YearMonth;

public record MesFinanceiro(
        YearMonth competencia,
        BigDecimal receitaPrevista,
        BigDecimal receitaRecebida,
        BigDecimal totalDespesas) {
}
