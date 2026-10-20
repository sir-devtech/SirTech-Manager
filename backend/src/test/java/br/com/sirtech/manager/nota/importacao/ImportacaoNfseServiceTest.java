package br.com.sirtech.manager.nota.importacao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.sirtech.manager.cliente.Cliente;
import br.com.sirtech.manager.cliente.ClienteService;
import br.com.sirtech.manager.cliente.Produto;
import br.com.sirtech.manager.nota.NotaFiscal;
import br.com.sirtech.manager.nota.NotaRepository;
import br.com.sirtech.manager.nota.StatusNota;
import br.com.sirtech.manager.nota.dto.NotaResponse;
import br.com.sirtech.manager.shared.excecao.ConflitoException;
import br.com.sirtech.manager.shared.excecao.RegraNegocioException;

@ExtendWith(MockitoExtension.class)
class ImportacaoNfseServiceTest {

    private static final YearMonth COMPETENCIA = YearMonth.of(2026, 10);
    private static final DadosNfse DADOS = new DadosNfse(
            "123", LocalDate.of(2026, 10, 5), COMPETENCIA, new BigDecimal("250.00"),
            "11222333000181", "12345678000195");

    @Mock
    private LeitorNfse leitor;

    @Mock
    private ClienteService clientes;

    @Mock
    private NotaRepository notas;

    private ImportacaoNfseService service;

    @BeforeEach
    void setUp() {
        service = new ImportacaoNfseService(leitor, clientes, notas, Clock.systemUTC(), "");
    }

    @Test
    void clienteDesconhecidoVira422() {
        when(leitor.ler(any())).thenReturn(DADOS);
        when(clientes.procurarPorDocumento("11222333000181")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.importar("<xml/>"))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("Cadastre o cliente");
    }

    @Test
    void notaPendenteRecebeValorENumeroDoXml() {
        Cliente cliente = cliente(true);
        NotaFiscal pendente = new NotaFiscal(cliente, COMPETENCIA, new BigDecimal("199.90"), LocalDate.of(2026, 10, 10));
        when(leitor.ler(any())).thenReturn(DADOS);
        when(clientes.procurarPorDocumento("11222333000181")).thenReturn(Optional.of(cliente));
        when(notas.filtrar(eq(COMPETENCIA), isNull(), isNull())).thenReturn(List.of(pendente));
        when(notas.saveAndFlush(any())).thenAnswer(invocacao -> invocacao.getArgument(0));

        NotaResponse resposta = service.importar("<xml/>");

        assertThat(resposta.status()).isEqualTo(StatusNota.EMITIDA);
        assertThat(resposta.numero()).isEqualTo("123");
        assertThat(resposta.valor()).isEqualByComparingTo("250.00");
        assertThat(pendente.getXmlOriginal()).isEqualTo("<xml/>");
    }

    @Test
    void segundaImportacaoComOutroNumeroVira409() {
        Cliente cliente = cliente(true);
        NotaFiscal emitida = new NotaFiscal(cliente, COMPETENCIA, new BigDecimal("250.00"), LocalDate.of(2026, 10, 10));
        emitida.emitir("99", LocalDate.of(2026, 10, 1));
        when(leitor.ler(any())).thenReturn(DADOS);
        when(clientes.procurarPorDocumento("11222333000181")).thenReturn(Optional.of(cliente));
        when(notas.filtrar(eq(COMPETENCIA), isNull(), isNull())).thenReturn(List.of(emitida));

        assertThatThrownBy(() -> service.importar("<xml/>"))
                .isInstanceOf(ConflitoException.class)
                .hasMessageContaining("outro número");
    }

    @Test
    void prestadorDiferenteDoCnpjConfiguradoVira422() {
        service = new ImportacaoNfseService(leitor, clientes, notas, Clock.systemUTC(), "11444777000161");
        when(leitor.ler(any())).thenReturn(DADOS);

        assertThatThrownBy(() -> service.importar("<xml/>"))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("Sir Tech");
    }

    private static Cliente cliente(boolean ativo) {
        Cliente cliente = new Cliente(
                "Barbearia X", "11222333000181", null, Produto.SIR_BARBER,
                new BigDecimal("199.90"), 10, LocalDateTime.parse("2026-10-01T09:00:00"));
        if (!ativo) {
            cliente.desativar();
        }
        return cliente;
    }
}
