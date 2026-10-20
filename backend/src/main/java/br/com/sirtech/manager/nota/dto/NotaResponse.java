package br.com.sirtech.manager.nota.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;

import br.com.sirtech.manager.nota.NotaFiscal;
import br.com.sirtech.manager.nota.StatusNota;

public record NotaResponse(
        Long id,
        Long clienteId,
        String clienteNome,
        YearMonth competencia,
        String numero,
        BigDecimal valor,
        LocalDate dataEmissao,
        LocalDate dataVencimento,
        LocalDate dataPagamento,
        StatusNota status,
        boolean vencida) {

    public static NotaResponse de(NotaFiscal nota, LocalDate hoje) {
        return new NotaResponse(
                nota.getId(),
                nota.getCliente().getId(),
                nota.getCliente().getNome(),
                nota.getCompetencia(),
                nota.getNumero(),
                nota.getValor(),
                nota.getDataEmissao(),
                nota.getDataVencimento(),
                nota.getDataPagamento(),
                nota.getStatus(),
                nota.isVencida(hoje));
    }
}
