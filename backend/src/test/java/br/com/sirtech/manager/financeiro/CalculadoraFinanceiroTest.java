package br.com.sirtech.manager.financeiro;

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
import br.com.sirtech.manager.financeiro.dto.FinanceiroResponse;
import br.com.sirtech.manager.nota.NotaFiscal;
import br.com.sirtech.manager.resumo.CalculadoraResumo;
import br.com.sirtech.manager.resumo.dto.ResumoResponse;

class CalculadoraFinanceiroTest {

    private static final YearMonth OUTUBRO = YearMonth.of(2026, 10);
    private static final LocalDate HOJE = LocalDate.of(2026, 10, 20);
    private final CalculadoraFinanceiro calculadora = new CalculadoraFinanceiro();
    private final CalculadoraResumo resumo = new CalculadoraResumo();
    private final Cliente barbearia = cliente("Barbearia", Produto.SIR_BARBER);
    private final Cliente pet = cliente("Pet shop", Produto.SIR_PET);

    @Test
    void umMesRepeteOResumoDoPainel() {
        NotaFiscal pendente = new NotaFiscal(barbearia, OUTUBRO, new BigDecimal("300.00"), LocalDate.of(2026, 10, 10));
        NotaFiscal paga = new NotaFiscal(pet, OUTUBRO, new BigDecimal("200.00"), LocalDate.of(2026, 10, 10));
        paga.emitir("1", LocalDate.of(2026, 10, 5));
        paga.pagar(LocalDate.of(2026, 10, 8));
        NotaFiscal cancelada = new NotaFiscal(barbearia, OUTUBRO, new BigDecimal("999.00"), LocalDate.of(2026, 10, 10));
        cancelada.cancelar();
        Despesa servidor = new Despesa("Servidor", CategoriaDespesa.SERVIDOR, new BigDecimal("150.00"), OUTUBRO, true);
        List<NotaFiscal> notas = List.of(pendente, paga, cancelada);

        FinanceiroResponse financeiro = calculadora.calcular(
                OUTUBRO, OUTUBRO, notas, List.of(servidor), List.of(), HOJE);
        ResumoResponse doPainel = resumo.calcular(OUTUBRO, notas, List.of(servidor), HOJE);

        assertThat(financeiro.receitaPrevista()).isEqualByComparingTo(doPainel.receitaPrevista());
        assertThat(financeiro.receitaRecebida()).isEqualByComparingTo(doPainel.receitaRecebida());
        assertThat(financeiro.totalDespesas()).isEqualByComparingTo(doPainel.totalDespesas());
        assertThat(financeiro.lucroPrevisto()).isEqualByComparingTo(doPainel.lucroPrevisto());
        assertThat(financeiro.lucroRealizado()).isEqualByComparingTo(doPainel.lucroRealizado());
        assertThat(financeiro.meses()).hasSize(1);
    }

    @Test
    void clienteComMaisDaMetadeFicaMarcadoEProdutoAgrupa() {
        NotaFiscal maior = new NotaFiscal(barbearia, OUTUBRO, new BigDecimal("300.00"), LocalDate.of(2026, 10, 10));
        NotaFiscal menor = new NotaFiscal(pet, OUTUBRO, new BigDecimal("200.00"), LocalDate.of(2026, 10, 10));

        FinanceiroResponse financeiro = calculadora.calcular(
                OUTUBRO, OUTUBRO, List.of(maior, menor), List.of(), List.of(), HOJE);

        assertThat(CalculadoraFinanceiro.concentrado(financeiro.porCliente())).isTrue();
        assertThat(financeiro.porCliente().getFirst().nome()).isEqualTo("Barbearia");
        assertThat(financeiro.porCliente().getFirst().parte()).isEqualByComparingTo("0.6000");
        assertThat(financeiro.porProduto()).extracting(fatia -> fatia.chave())
                .containsExactly("SIR_BARBER", "SIR_PET");
    }

    @Test
    void atrasoDeHojeNaoContaComoVencidaEBateComOResumoDoMes() {
        NotaFiscal venceHoje = new NotaFiscal(barbearia, OUTUBRO, new BigDecimal("300.00"), HOJE);
        venceHoje.emitir("2", LocalDate.of(2026, 10, 1));
        NotaFiscal atrasada = new NotaFiscal(pet, OUTUBRO, new BigDecimal("200.00"), HOJE.minusDays(10));
        atrasada.emitir("3", LocalDate.of(2026, 10, 1));
        List<NotaFiscal> abertas = List.of(venceHoje, atrasada);

        FinanceiroResponse financeiro = calculadora.calcular(
                OUTUBRO, OUTUBRO, abertas, List.of(), abertas, HOJE);
        ResumoResponse doPainel = resumo.calcular(OUTUBRO, abertas, List.of(), HOJE);

        assertThat(financeiro.atraso().aVencer()).isEqualTo(1);
        assertThat(financeiro.atraso().ate30()).isEqualTo(1);
        assertThat(financeiro.atraso().ate30()).isEqualTo(doPainel.notasVencidas());
        assertThat(financeiro.atraso().valorAte30()).isEqualByComparingTo("200.00");
    }

    @Test
    void mesSemMovimentoContinuaNaSerie() {
        FinanceiroResponse financeiro = calculadora.calcular(
                YearMonth.of(2026, 8), OUTUBRO, List.of(), List.of(), List.of(), HOJE);

        assertThat(financeiro.meses()).extracting(mes -> mes.competencia().toString())
                .containsExactly("2026-08", "2026-09", "2026-10");
        assertThat(financeiro.receitaPrevista()).isEqualByComparingTo("0.00");
    }

    private static Cliente cliente(String nome, Produto produto) {
        return new Cliente(nome, "11222333000181", null, produto, new BigDecimal("100.00"), 10, LocalDateTime.now());
    }
}
