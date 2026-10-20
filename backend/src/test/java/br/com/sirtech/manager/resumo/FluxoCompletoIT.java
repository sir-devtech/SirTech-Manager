package br.com.sirtech.manager.resumo;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.jayway.jsonpath.JsonPath;

import br.com.sirtech.manager.AbstractIntegrationTest;

/** Cliente, nota gerada, emitida, paga, despesa e o resumo com os valores certos. */
@AutoConfigureMockMvc
class FluxoCompletoIT extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbc;

    @AfterEach
    void limpar() {
        jdbc.update("DELETE FROM despesa");
        jdbc.update("DELETE FROM nota_fiscal");
        jdbc.update("DELETE FROM cliente");
    }

    @Test
    void resumoDoMesBateComNotaPagaEDespesa() throws Exception {
        mockMvc.perform(post("/api/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Barbearia X","documento":"11.222.333/0001-81",
                                 "produto":"SIR_BARBER","valorMensal":199.90,"diaVencimento":10}
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/notas/gerar").param("competencia", "2026-10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.notasCriadas").value(1));

        MvcResult lista = mockMvc.perform(get("/api/notas").param("competencia", "2026-10"))
                .andExpect(status().isOk())
                .andReturn();
        Number notaId = JsonPath.read(lista.getResponse().getContentAsString(), "$[0].id");

        mockMvc.perform(patch("/api/notas/" + notaId + "/emitir")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"numero":"NF-1","dataEmissao":"2026-10-05"}
                                """))
                .andExpect(status().isOk());
        mockMvc.perform(patch("/api/notas/" + notaId + "/pagar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"dataPagamento":"2026-10-10"}
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/despesas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"descricao":"Contador","categoria":"CONTADOR","valor":350.00,
                                 "competencia":"2026-10","recorrente":true}
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/resumo").param("competencia", "2026-10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.competencia").value("2026-10"))
                .andExpect(jsonPath("$.receitaPrevista").value(199.90))
                .andExpect(jsonPath("$.receitaRecebida").value(199.90))
                .andExpect(jsonPath("$.totalDespesas").value(350.00))
                .andExpect(jsonPath("$.lucroPrevisto").value(-150.10))
                .andExpect(jsonPath("$.lucroRealizado").value(-150.10))
                .andExpect(jsonPath("$.notasPendentesEmissao").value(0))
                .andExpect(jsonPath("$.notasVencidas").value(0));
    }
}
