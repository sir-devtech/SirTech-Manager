package br.com.sirtech.manager.nota;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;

import org.junit.jupiter.api.Test;

import br.com.sirtech.manager.cliente.Cliente;
import br.com.sirtech.manager.cliente.Produto;
import br.com.sirtech.manager.shared.excecao.RegraNegocioException;
import br.com.sirtech.manager.shared.excecao.TransicaoInvalidaException;

class NotaFiscalTest {

    private final Cliente cliente = new Cliente("Barbearia X", "11222333000181", null,
            Produto.SIR_BARBER, new BigDecimal("199.90"), 10, LocalDateTime.now());

    @Test
    void nascePendenteEPodeSerEmitidaPaga() {
        NotaFiscal nota = nova();

        assertThat(nota.getStatus()).isEqualTo(StatusNota.PENDENTE);

        nota.emitir("NF-1", LocalDate.of(2026, 10, 5));
        assertThat(nota.getStatus()).isEqualTo(StatusNota.EMITIDA);
        assertThat(nota.getNumero()).isEqualTo("NF-1");

        nota.pagar(LocalDate.of(2026, 10, 10));
        assertThat(nota.getStatus()).isEqualTo(StatusNota.PAGA);
        assertThat(nota.getDataPagamento()).isEqualTo(LocalDate.of(2026, 10, 10));
    }

    @Test
    void pagaNaoMudaMais() {
        NotaFiscal nota = nova();
        nota.emitir("NF-1", LocalDate.of(2026, 10, 5));
        nota.pagar(LocalDate.of(2026, 10, 10));

        assertThatThrownBy(nota::cancelar).isInstanceOf(TransicaoInvalidaException.class);
        assertThat(nota.getStatus()).isEqualTo(StatusNota.PAGA);
    }

    @Test
    void pagamentoAnteriorAEmissaoEhRecusado() {
        NotaFiscal nota = nova();
        nota.emitir("NF-1", LocalDate.of(2026, 10, 5));

        assertThatThrownBy(() -> nota.pagar(LocalDate.of(2026, 10, 1)))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("anterior");
        assertThat(nota.getStatus()).isEqualTo(StatusNota.EMITIDA);
    }

    @Test
    void emitirSemNumeroEhRecusado() {
        assertThatThrownBy(() -> nova().emitir("  ", LocalDate.of(2026, 10, 5)))
                .isInstanceOf(RegraNegocioException.class);
    }

    @Test
    void vencidaSoQuandoEmitidaEComVencimentoPassado() {
        NotaFiscal nota = nova();
        LocalDate depoisDoVencimento = LocalDate.of(2026, 10, 11);

        assertThat(nota.isVencida(depoisDoVencimento)).isFalse();

        nota.emitir("NF-1", LocalDate.of(2026, 10, 1));
        assertThat(nota.isVencida(depoisDoVencimento)).isTrue();
        assertThat(nota.isVencida(LocalDate.of(2026, 10, 10))).isFalse();

        nota.pagar(LocalDate.of(2026, 10, 12));
        assertThat(nota.isVencida(depoisDoVencimento)).isFalse();
    }

    @Test
    void pendenteOuEmitidaPodemSerCanceladas() {
        NotaFiscal pendente = nova();
        pendente.cancelar();
        assertThat(pendente.getStatus()).isEqualTo(StatusNota.CANCELADA);

        NotaFiscal emitida = nova();
        emitida.emitir("NF-2", LocalDate.of(2026, 10, 2));
        emitida.cancelar();
        assertThat(emitida.getStatus()).isEqualTo(StatusNota.CANCELADA);
    }

    private NotaFiscal nova() {
        return new NotaFiscal(cliente, YearMonth.of(2026, 10), new BigDecimal("199.90"), LocalDate.of(2026, 10, 10));
    }
}
