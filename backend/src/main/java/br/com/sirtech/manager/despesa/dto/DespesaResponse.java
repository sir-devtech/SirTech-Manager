package br.com.sirtech.manager.despesa.dto;

import java.math.BigDecimal;
import java.time.YearMonth;

import br.com.sirtech.manager.despesa.CategoriaDespesa;
import br.com.sirtech.manager.despesa.Despesa;

public record DespesaResponse(
        Long id,
        String descricao,
        CategoriaDespesa categoria,
        BigDecimal valor,
        YearMonth competencia,
        boolean recorrente) {

    public static DespesaResponse de(Despesa despesa) {
        return new DespesaResponse(
                despesa.getId(),
                despesa.getDescricao(),
                despesa.getCategoria(),
                despesa.getValor(),
                despesa.getCompetencia(),
                despesa.isRecorrente());
    }
}
