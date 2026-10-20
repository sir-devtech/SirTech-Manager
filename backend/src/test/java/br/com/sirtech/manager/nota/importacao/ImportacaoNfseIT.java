package br.com.sirtech.manager.nota.importacao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.jayway.jsonpath.JsonPath;

import br.com.sirtech.manager.AbstractIntegrationTest;

@AutoConfigureMockMvc
class ImportacaoNfseIT extends AbstractIntegrationTest {

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
    void importaXmlERepetirNaoDuplica() throws Exception {
        criarCliente();
        MockMultipartFile arquivo = xml();

        MvcResult primeira = mockMvc.perform(multipart("/api/notas/importar").file(arquivo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numero").value("123"))
                .andExpect(jsonPath("$.status").value("EMITIDA"))
                .andExpect(jsonPath("$.valor").value(250.00))
                .andReturn();
        Number id = JsonPath.read(primeira.getResponse().getContentAsString(), "$.id");

        mockMvc.perform(multipart("/api/notas/importar").file(arquivo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id));

        Integer quantidade = jdbc.queryForObject("SELECT COUNT(*) FROM nota_fiscal", Integer.class);
        assertThat(quantidade).isEqualTo(1);
    }

    @Test
    void clienteDesconhecidoRetorna422() throws Exception {
        mockMvc.perform(multipart("/api/notas/importar").file(xml()))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.detail").value(containsString("Cadastre o cliente")));
    }

    @Test
    void notaCanceladaNaoBloqueiaNovaImportacao() throws Exception {
        Number clienteId = criarCliente();
        MvcResult criada = mockMvc.perform(post("/api/notas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"clienteId":%s,"competencia":"2026-10"}
                                """.formatted(clienteId)))
                .andExpect(status().isCreated())
                .andReturn();
        Number id = JsonPath.read(criada.getResponse().getContentAsString(), "$.id");
        mockMvc.perform(patch("/api/notas/" + id + "/cancelar")).andExpect(status().isOk());

        mockMvc.perform(multipart("/api/notas/importar").file(xml()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("EMITIDA"))
                .andExpect(jsonPath("$.numero").value("123"));
    }

    private MockMultipartFile xml() throws Exception {
        byte[] conteudo = new ClassPathResource("nfse/nfse-ficticia.xml").getContentAsByteArray();
        return new MockMultipartFile("arquivo", "nfse.xml", "application/xml", conteudo);
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
