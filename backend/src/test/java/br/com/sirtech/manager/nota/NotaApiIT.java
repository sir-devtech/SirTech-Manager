package br.com.sirtech.manager.nota;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.jayway.jsonpath.JsonPath;

import br.com.sirtech.manager.AbstractIntegrationTest;

@AutoConfigureMockMvc
class NotaApiIT extends AbstractIntegrationTest {

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
    void notaDuplicadaNaMesmaCompetenciaRetorna409() throws Exception {
        Number clienteId = criarCliente();
        String nota = """
                {"clienteId":%s,"competencia":"2026-10"}
                """.formatted(clienteId);

        mockMvc.perform(post("/api/notas").contentType(MediaType.APPLICATION_JSON).content(nota))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/notas").contentType(MediaType.APPLICATION_JSON).content(nota))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.detail").value("Já existe uma nota ativa deste cliente nesta competência"));
    }

    private Number criarCliente() throws Exception {
        MvcResult resultado = mockMvc.perform(post("/api/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Barbearia X","documento":"11.222.333/0001-81",
                                 "produto":"SIR_BARBER","valorMensal":199.90,"diaVencimento":10}
                                """))
                .andExpect(status().isCreated())
                .andReturn();
        return JsonPath.read(resultado.getResponse().getContentAsString(), "$.id");
    }
}
