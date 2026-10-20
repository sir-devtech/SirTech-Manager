package br.com.sirtech.manager.financeiro;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import java.time.Clock;
import java.time.YearMonth;

import org.junit.jupiter.api.Test;

import br.com.sirtech.manager.despesa.DespesaRepository;
import br.com.sirtech.manager.nota.NotaRepository;
import br.com.sirtech.manager.shared.excecao.RegraNegocioException;

class FinanceiroServiceTest {

    private final FinanceiroService service = new FinanceiroService(
            mock(NotaRepository.class), mock(DespesaRepository.class), Clock.systemUTC());

    @Test
    void recusaPeriodoInvertido() {
        assertThatThrownBy(() -> service.consultar(YearMonth.of(2026, 10), YearMonth.of(2026, 1)))
                .isInstanceOf(RegraNegocioException.class);
    }

    @Test
    void recusaPeriodoMaiorQueTrintaESeisMeses() {
        assertThatThrownBy(() -> service.consultar(YearMonth.of(2020, 1), YearMonth.of(2026, 1)))
                .isInstanceOf(RegraNegocioException.class);
    }
}
