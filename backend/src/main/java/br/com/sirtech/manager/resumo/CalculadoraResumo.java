package br.com.sirtech.manager.resumo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import br.com.sirtech.manager.despesa.Despesa;
import br.com.sirtech.manager.nota.NotaFiscal;
import br.com.sirtech.manager.nota.StatusNota;
import br.com.sirtech.manager.resumo.dto.ResumoResponse;

/**
 * Contas do mês, sem banco e sem Spring. Cancelada fica fora da receita.
 * Vencida só conta se já foi emitida e o vencimento passou.
 */
public class CalculadoraResumo {

    private static final BigDecimal ZERO = new BigDecimal("0.00");

    public ResumoResponse calcular(YearMonth competencia, List<NotaFiscal> notas, List<Despesa> despesas, LocalDate hoje) {
        BigDecimal prevista = ZERO;
        BigDecimal recebida = ZERO;
        int pendentes = 0;
        int vencidas = 0;
        for (NotaFiscal nota : notas) {
            if (nota.getStatus() != StatusNota.CANCELADA) {
                prevista = prevista.add(nota.getValor());
            }
            if (nota.getStatus() == StatusNota.PAGA) {
                recebida = recebida.add(nota.getValor());
            }
            if (nota.getStatus() == StatusNota.PENDENTE) {
                pendentes++;
            }
            if (nota.isVencida(hoje)) {
                vencidas++;
            }
        }
        BigDecimal totalDespesas = despesas.stream().map(Despesa::getValor).reduce(ZERO, BigDecimal::add);
        return new ResumoResponse(
                competencia,
                prevista,
                recebida,
                totalDespesas,
                prevista.subtract(totalDespesas),
                recebida.subtract(totalDespesas),
                pendentes,
                vencidas);
    }
}
