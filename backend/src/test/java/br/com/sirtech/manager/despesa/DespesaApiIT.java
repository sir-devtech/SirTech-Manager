package br.com.sirtech.manager.despesa;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
class DespesaApiIT extends AbstractIntegrationTest {

    private static final String CONTADOR = """
            {"descricao":"Contador","categoria":"CONTADOR","valor":350.00,
             "competencia":"2026-10","recorrente":true}
            """;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbc;

    @AfterEach
    void limpar() {
        jdbc.update("DELETE FROM despesa");
    }

    @Test
    void crudEFiltroPorCompetencia() throws Exception {
        MvcResult criada = mockMvc.perform(post("/api/despesas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CONTADOR))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.recorrente").value(true))
                .andReturn();
        Number id = JsonPath.read(criada.getResponse().getContentAsString(), "$.id");

        mockMvc.perform(get("/api/despesas").param("competencia", "2026-10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
        mockMvc.perform(get("/api/despesas").param("competencia", "2026-11"))
                .andExpect(jsonPath("$.length()").value(0));

        mockMvc.perform(put("/api/despesas/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"descricao":"Contador","categoria":"CONTADOR","valor":400.00,
                                 "competencia":"2026-10","recorrente":true}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valor").value(400.00));

        mockMvc.perform(delete("/api/despesas/" + id))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/despesas").param("competencia", "2026-10"))
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void recorrenteDuplicadaRetorna409EAvulsaPodeRepetir() throws Exception {
        criar(CONTADOR).andExpect(status().isCreated());
        criar(CONTADOR)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("Já existe uma despesa recorrente igual nesta competência"));

        String avulsa = CONTADOR.replace("\"recorrente\":true", "\"recorrente\":false");
        criar(avulsa).andExpect(status().isCreated());
        criar(avulsa).andExpect(status().isCreated());
    }

    private org.springframework.test.web.servlet.ResultActions criar(String json) throws Exception {
        return mockMvc.perform(post("/api/despesas").contentType(MediaType.APPLICATION_JSON).content(json));
    }
}
