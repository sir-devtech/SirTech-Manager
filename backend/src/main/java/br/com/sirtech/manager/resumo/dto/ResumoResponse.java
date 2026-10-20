package br.com.sirtech.manager.resumo.dto;

import java.math.BigDecimal;
import java.time.YearMonth;

public record ResumoResponse(
        YearMonth competencia,
        BigDecimal receitaPrevista,
        BigDecimal receitaRecebida,
        BigDecimal totalDespesas,
        BigDecimal lucroPrevisto,
        BigDecimal lucroRealizado,
        int notasPendentesEmissao,
        int notasVencidas) {
}
