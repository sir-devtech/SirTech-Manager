package br.com.sirtech.manager.cliente;

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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import br.com.sirtech.manager.AbstractIntegrationTest;

/** API de ponta a ponta: HTTP (MockMvc) -> controller -> service -> PostgreSQL real. */
@AutoConfigureMockMvc
class ClienteApiIT extends AbstractIntegrationTest {

    private static final String BARBEARIA = """
            {"nome":"Barbearia X","documento":"11.222.333/0001-81","email":"x@barbearia.example",
             "produto":"SIR_BARBER","valorMensal":199.90,"diaVencimento":10}
            """;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ClienteRepository repository;

    @AfterEach
    void limpar() {
        repository.deleteAll();
    }

    @Test
    void documentoDuplicadoRetorna409ComMensagemClara() throws Exception {
        criar(BARBEARIA).andExpect(status().isCreated());

        criar(BARBEARIA)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.detail").value("Já existe um cliente com este CPF/CNPJ"));
    }

    @Test
    void desativarNaoApagaEFiltroPorAtivoFunciona() throws Exception {
        String location = criar(BARBEARIA).andReturn().getResponse().getHeader("Location");

        mockMvc.perform(patch(location + "/desativar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ativo").value(false));

        mockMvc.perform(get("/api/clientes").param("ativo", "true"))
                .andExpect(jsonPath("$.length()").value(0));
        mockMvc.perform(get("/api/clientes").param("ativo", "false"))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].documento").value("11222333000181"));
    }

    private ResultActions criar(String json) throws Exception {
        return mockMvc.perform(post("/api/clientes").contentType(MediaType.APPLICATION_JSON).content(json));
    }
}
