package br.com.sirtech.manager.resumo;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

import org.junit.jupiter.api.Test;

import br.com.sirtech.manager.cliente.Cliente;
import br.com.sirtech.manager.cliente.Produto;
import br.com.sirtech.manager.despesa.CategoriaDespesa;
import br.com.sirtech.manager.despesa.Despesa;
import br.com.sirtech.manager.nota.NotaFiscal;
import br.com.sirtech.manager.resumo.dto.ResumoResponse;

class CalculadoraResumoTest {

    private static final YearMonth MES = YearMonth.of(2026, 10);
    private static final LocalDate HOJE = LocalDate.of(2026, 10, 17);
    private final CalculadoraResumo calculadora = new CalculadoraResumo();
    private final Cliente cliente = new Cliente("Barbearia X", "11222333000181", null,
            Produto.SIR_BARBER, new BigDecimal("199.90"), 10, LocalDateTime.now());

    @Test
    void mesVazioZeraTudo() {
        ResumoResponse resumo = calculadora.calcular(MES, List.of(), List.of(), HOJE);

        assertThat(resumo.receitaPrevista()).isEqualByComparingTo("0.00");
        assertThat(resumo.receitaRecebida()).isEqualByComparingTo("0.00");
        assertThat(resumo.totalDespesas()).isEqualByComparingTo("0.00");
        assertThat(resumo.lucroPrevisto()).isEqualByComparingTo("0.00");
        assertThat(resumo.lucroRealizado()).isEqualByComparingTo("0.00");
        assertThat(resumo.notasPendentesEmissao()).isZero();
        assertThat(resumo.notasVencidas()).isZero();
    }

    @Test
    void somaReceitaDespesasLucrosEIgnoraCancelada() {
        NotaFiscal pendente = nota("100.00");
        NotaFiscal vencida = nota("50.00");
        vencida.emitir("NF-1", LocalDate.of(2026, 10, 2));
        NotaFiscal paga = nota("30.00");
        paga.emitir("NF-2", LocalDate.of(2026, 10, 3));
        paga.pagar(LocalDate.of(2026, 10, 4));
        NotaFiscal cancelada = nota("999.00");
        cancelada.cancelar();
        Despesa contador = new Despesa("Contador", CategoriaDespesa.CONTADOR,
                new BigDecimal("20.00"), MES, true);

        ResumoResponse resumo = calculadora.calcular(
                MES, List.of(pendente, vencida, paga, cancelada), List.of(contador), HOJE);

        assertThat(resumo.receitaPrevista()).isEqualByComparingTo("180.00");
        assertThat(resumo.receitaRecebida()).isEqualByComparingTo("30.00");
        assertThat(resumo.totalDespesas()).isEqualByComparingTo("20.00");
        assertThat(resumo.lucroPrevisto()).isEqualByComparingTo("160.00");
        assertThat(resumo.lucroRealizado()).isEqualByComparingTo("10.00");
        assertThat(resumo.notasPendentesEmissao()).isEqualTo(1);
        assertThat(resumo.notasVencidas()).isEqualTo(1);
    }

    private NotaFiscal nota(String valor) {
        return new NotaFiscal(cliente, MES, new BigDecimal(valor), LocalDate.of(2026, 10, 1));
    }
}
