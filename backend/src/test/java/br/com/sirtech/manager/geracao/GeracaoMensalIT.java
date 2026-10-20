package br.com.sirtech.manager.geracao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import br.com.sirtech.manager.AbstractIntegrationTest;
import br.com.sirtech.manager.cliente.Cliente;
import br.com.sirtech.manager.cliente.ClienteRepository;
import br.com.sirtech.manager.cliente.Produto;
import br.com.sirtech.manager.despesa.CategoriaDespesa;
import br.com.sirtech.manager.despesa.Despesa;
import br.com.sirtech.manager.despesa.DespesaRepository;
import br.com.sirtech.manager.nota.NotaFiscal;
import br.com.sirtech.manager.nota.NotaRepository;
import br.com.sirtech.manager.nota.StatusNota;

@AutoConfigureMockMvc
class GeracaoMensalIT extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private GeracaoMensalService service;

    @Autowired
    private ClienteRepository clientes;

    @Autowired
    private NotaRepository notas;

    @Autowired
    private DespesaRepository despesas;

    @Autowired
    private JdbcTemplate jdbc;

    @AfterEach
    void limpar() {
        jdbc.update("DELETE FROM despesa");
        jdbc.update("DELETE FROM nota_fiscal");
        jdbc.update("DELETE FROM cliente");
    }

    @Test
    void rodarDuasVezesNaoDuplicaNotasNemDespesas() throws Exception {
        Cliente ativo = clientes.saveAndFlush(cliente("Barbearia X", "11222333000181", true, 10, "199.90"));
        Cliente inativo = clientes.saveAndFlush(cliente("Pet Y", "11444777000161", false, 15, "80.00"));
        despesas.saveAndFlush(new Despesa("Contador", CategoriaDespesa.CONTADOR,
                new BigDecimal("350.00"), YearMonth.of(2026, 9), true));

        mockMvc.perform(post("/api/notas/gerar").param("competencia", "2026-10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.notasCriadas").value(1))
                .andExpect(jsonPath("$.despesasReplicadas").value(1));
        mockMvc.perform(post("/api/notas/gerar").param("competencia", "2026-10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.notasCriadas").value(0))
                .andExpect(jsonPath("$.despesasReplicadas").value(0));

        assertThat(notas.filtrar(YearMonth.of(2026, 10), null, ativo.getId()))
                .singleElement()
                .satisfies(nota -> {
                    assertThat(nota.getStatus()).isEqualTo(StatusNota.PENDENTE);
                    assertThat(nota.getValor()).isEqualByComparingTo("199.90");
                    assertThat(nota.getDataVencimento()).isEqualTo(LocalDate.of(2026, 10, 10));
                });
        assertThat(notas.filtrar(YearMonth.of(2026, 10), null, inativo.getId())).isEmpty();
        assertThat(despesas.findByCompetenciaOrderByDescricaoAsc(YearMonth.of(2026, 10)))
                .singleElement()
                .satisfies(despesa -> {
                    assertThat(despesa.getDescricao()).isEqualTo("Contador");
                    assertThat(despesa.isRecorrente()).isTrue();
                    assertThat(despesa.getValor()).isEqualByComparingTo("350.00");
                });
    }

    @Test
    void despesaRecorrentePulaUmMesSemQuebrar() {
        clientes.saveAndFlush(cliente("Barbearia X", "11222333000181", true, 10, "199.90"));
        despesas.saveAndFlush(new Despesa("Contador", CategoriaDespesa.CONTADOR,
                new BigDecimal("350.00"), YearMonth.of(2026, 8), true));

        ResultadoGeracao resultado = service.gerar(YearMonth.of(2026, 10));

        assertThat(resultado.despesasReplicadas()).isEqualTo(1);
        assertThat(despesas.findByCompetenciaOrderByDescricaoAsc(YearMonth.of(2026, 9))).isEmpty();
        assertThat(despesas.findByCompetenciaOrderByDescricaoAsc(YearMonth.of(2026, 10))).hasSize(1);
    }

    @Test
    void notaCanceladaEhRecriadaComoPendente() {
        Cliente cliente = clientes.saveAndFlush(cliente("Barbearia X", "11222333000181", true, 10, "199.90"));
        NotaFiscal cancelada = notas.saveAndFlush(new NotaFiscal(
                cliente, YearMonth.of(2026, 10), new BigDecimal("199.90"), LocalDate.of(2026, 10, 10)));
        cancelada.cancelar();
        notas.saveAndFlush(cancelada);

        ResultadoGeracao resultado = service.gerar(YearMonth.of(2026, 10));

        assertThat(resultado.notasCriadas()).isEqualTo(1);
        assertThat(notas.filtrar(YearMonth.of(2026, 10), null, cliente.getId()))
                .extracting(NotaFiscal::getStatus)
                .containsExactlyInAnyOrder(StatusNota.CANCELADA, StatusNota.PENDENTE);
    }

    private static Cliente cliente(String nome, String documento, boolean ativo, int dia, String valor) {
        Cliente cliente = new Cliente(nome, documento, null, Produto.SIR_BARBER,
                new BigDecimal(valor), dia, LocalDateTime.of(2026, 10, 1, 9, 0));
        if (!ativo) {
            cliente.desativar();
        }
        return cliente;
    }
}
