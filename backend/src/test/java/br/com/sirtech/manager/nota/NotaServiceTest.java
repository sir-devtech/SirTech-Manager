package br.com.sirtech.manager.nota;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.sirtech.manager.cliente.Cliente;
import br.com.sirtech.manager.cliente.ClienteService;
import br.com.sirtech.manager.cliente.Produto;
import br.com.sirtech.manager.nota.dto.NotaRequest;
import br.com.sirtech.manager.shared.excecao.RegraNegocioException;

@ExtendWith(MockitoExtension.class)
class NotaServiceTest {

    private static final Clock CLOCK = Clock.fixed(
            Instant.parse("2026-10-17T12:00:00Z"), ZoneId.of("America/Sao_Paulo"));

    @Mock
    private NotaRepository repository;

    @Mock
    private ClienteService clienteService;

    private NotaService service;

    @BeforeEach
    void setUp() {
        service = new NotaService(repository, clienteService, CLOCK);
    }

    @Test
    void criarUsaValorMensalECalculaVencimentoNoDiaDoCliente() {
        Cliente cliente = cliente(true, 10, "199.90");
        when(clienteService.buscarEntidade(1L)).thenReturn(cliente);
        when(repository.saveAndFlush(any(NotaFiscal.class))).thenAnswer(inv -> inv.getArgument(0));

        service.criar(new NotaRequest(1L, YearMonth.of(2026, 2), null));

        ArgumentCaptor<NotaFiscal> captor = ArgumentCaptor.forClass(NotaFiscal.class);
        verify(repository).saveAndFlush(captor.capture());
        NotaFiscal salva = captor.getValue();
        assertThat(salva.getValor()).isEqualByComparingTo("199.90");
        assertThat(salva.getDataVencimento()).isEqualTo(LocalDate.of(2026, 2, 10));
        assertThat(salva.getCompetencia()).isEqualTo(YearMonth.of(2026, 2));
        assertThat(salva.getStatus()).isEqualTo(StatusNota.PENDENTE);
    }

    @Test
    void criarDeClienteInativoLanca422() {
        when(clienteService.buscarEntidade(1L)).thenReturn(cliente(false, 10, "199.90"));

        assertThatThrownBy(() -> service.criar(new NotaRequest(1L, YearMonth.of(2026, 10), null)))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("inativo");
    }

    @Test
    void realinharPendentesAtualizaValorEVencimento() {
        Cliente cliente = cliente(true, 10, "300.00");
        NotaFiscal pendente = new NotaFiscal(
                cliente, YearMonth.of(2026, 10), new BigDecimal("199.90"), LocalDate.of(2026, 10, 15));
        when(repository.filtrar(isNull(), eq(StatusNota.PENDENTE), isNull())).thenReturn(List.of(pendente));

        service.realinharPendentes(cliente);

        assertThat(pendente.getValor()).isEqualByComparingTo("300.00");
        assertThat(pendente.getDataVencimento()).isEqualTo(LocalDate.of(2026, 10, 10));
    }

    private static Cliente cliente(boolean ativo, int dia, String valor) {
        Cliente cliente = new Cliente("Barbearia X", "11222333000181", null, Produto.SIR_BARBER,
                new BigDecimal(valor), dia, LocalDateTime.now(CLOCK));
        if (!ativo) {
            cliente.desativar();
        }
        return cliente;
    }
}
