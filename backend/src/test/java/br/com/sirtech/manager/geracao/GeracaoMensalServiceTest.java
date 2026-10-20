package br.com.sirtech.manager.geracao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.sirtech.manager.cliente.ClienteService;
import br.com.sirtech.manager.cliente.Produto;
import br.com.sirtech.manager.cliente.dto.ClienteResponse;
import br.com.sirtech.manager.despesa.DespesaService;
import br.com.sirtech.manager.nota.NotaService;

@ExtendWith(MockitoExtension.class)
class GeracaoMensalServiceTest {

    @Mock
    private ClienteService clientes;

    @Mock
    private NotaService notas;

    @Mock
    private DespesaService despesas;

    private GeracaoMensalService service;

    @BeforeEach
    void setUp() {
        service = new GeracaoMensalService(clientes, notas, despesas);
    }

    @Test
    void ignoraInativoUsaValorMensalECompetenciaPedida() {
        ClienteResponse ativo = cliente(1L, true, "199.90", 10);
        ClienteResponse inativo = cliente(2L, false, "50.00", 5);
        when(clientes.listar(true)).thenReturn(List.of(ativo, inativo));
        when(notas.inserirPendenteSeAusente(1L, YearMonth.of(2026, 10), new BigDecimal("199.90"), 10)).thenReturn(1);
        when(despesas.replicarRecorrentes(YearMonth.of(2026, 10))).thenReturn(2);

        ResultadoGeracao resultado = service.gerar(YearMonth.of(2026, 10));

        assertThat(resultado.notasCriadas()).isEqualTo(1);
        assertThat(resultado.despesasReplicadas()).isEqualTo(2);
        verify(notas).inserirPendenteSeAusente(1L, YearMonth.of(2026, 10), new BigDecimal("199.90"), 10);
        verify(notas, never()).inserirPendenteSeAusente(
                org.mockito.ArgumentMatchers.eq(2L),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.anyInt());
    }

    private static ClienteResponse cliente(Long id, boolean ativo, String valor, int dia) {
        return new ClienteResponse(id, "Cliente " + id, "11222333000181", null, Produto.SIR_BARBER,
                new BigDecimal(valor), dia, ativo, LocalDateTime.of(2026, 10, 1, 9, 0));
    }
}
