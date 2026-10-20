package br.com.sirtech.manager.financeiro.dto;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

public record FinanceiroResponse(
        YearMonth inicio,
        YearMonth fim,
        BigDecimal receitaPrevista,
        BigDecimal receitaRecebida,
        BigDecimal totalDespesas,
        BigDecimal lucroPrevisto,
        BigDecimal lucroRealizado,
        List<MesFinanceiro> meses,
        List<Fatia> porCliente,
        List<Fatia> porProduto,
        List<Fatia> porCategoria,
        FaixaAtraso atraso) {
}
