package br.com.sirtech.manager.financeiro;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import br.com.sirtech.manager.AbstractIntegrationTest;
import br.com.sirtech.manager.cliente.Cliente;
import br.com.sirtech.manager.cliente.ClienteRepository;
import br.com.sirtech.manager.cliente.Produto;
import br.com.sirtech.manager.despesa.CategoriaDespesa;
import br.com.sirtech.manager.despesa.Despesa;
import br.com.sirtech.manager.despesa.DespesaRepository;
import br.com.sirtech.manager.geracao.GeracaoMensalService;
import br.com.sirtech.manager.nota.NotaFiscal;
import br.com.sirtech.manager.nota.NotaRepository;

/** A rota do período, no PostgreSQL, conversa com o resumo do mês e com a geração. */
@AutoConfigureMockMvc
class FinanceiroPeriodoIT extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private GeracaoMensalService geracao;

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
    void periodoDeDoisMesesBateComOResumoENaoCopiaDespesaAvulsa() throws Exception {
        Cliente cliente = clientes.saveAndFlush(new Cliente(
                "Barbearia X", "11222333000181", null, Produto.SIR_BARBER,
                new BigDecimal("300.00"), 10, LocalDateTime.of(2026, 9, 1, 9, 0)));
        NotaFiscal paga = new NotaFiscal(cliente, java.time.YearMonth.of(2026, 9),
                new BigDecimal("300.00"), LocalDate.of(2026, 9, 10));
        paga.emitir("NF-9", LocalDate.of(2026, 9, 5));
        paga.pagar(LocalDate.of(2026, 9, 10));
        notas.saveAndFlush(paga);
        despesas.saveAndFlush(new Despesa(
                "Servidor DigitalOcean (demo)", CategoriaDespesa.SERVIDOR,
                new BigDecimal("150.00"), java.time.YearMonth.of(2026, 9), true));
        despesas.saveAndFlush(new Despesa(
                "Simples Nacional (demo)", CategoriaDespesa.IMPOSTO,
                new BigDecimal("90.00"), java.time.YearMonth.of(2026, 9), false));

        geracao.gerar(java.time.YearMonth.of(2026, 10));
        geracao.gerar(java.time.YearMonth.of(2026, 10));

        mockMvc.perform(get("/api/resumo").param("competencia", "2026-10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.receitaPrevista").value(300.00))
                .andExpect(jsonPath("$.totalDespesas").value(150.00));

        mockMvc.perform(get("/api/financeiro").param("inicio", "2026-09").param("fim", "2026-10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.meses.length()").value(2))
                .andExpect(jsonPath("$.meses[0].competencia").value("2026-09"))
                .andExpect(jsonPath("$.meses[0].receitaRecebida").value(300.00))
                .andExpect(jsonPath("$.meses[0].totalDespesas").value(240.00))
                .andExpect(jsonPath("$.meses[1].competencia").value("2026-10"))
                .andExpect(jsonPath("$.meses[1].receitaPrevista").value(300.00))
                .andExpect(jsonPath("$.meses[1].totalDespesas").value(150.00))
                .andExpect(jsonPath("$.receitaPrevista").value(600.00))
                .andExpect(jsonPath("$.totalDespesas").value(390.00));

        mockMvc.perform(post("/api/notas/gerar").param("competencia", "2026-10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.notasCriadas").value(0))
                .andExpect(jsonPath("$.despesasReplicadas").value(0));
    }
}
