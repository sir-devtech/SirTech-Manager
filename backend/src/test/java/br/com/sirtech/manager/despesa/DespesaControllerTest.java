package br.com.sirtech.manager.despesa;

import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import br.com.sirtech.manager.shared.config.AnoMesWebConfig;
import br.com.sirtech.manager.shared.excecao.RecursoNaoEncontradoException;

@WebMvcTest(DespesaController.class)
@Import(AnoMesWebConfig.class)
class DespesaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DespesaService service;

    @Test
    void criarComDadosInvalidosRetorna400() throws Exception {
        mockMvc.perform(post("/api/despesas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"descricao":"","valor":0,"recorrente":true}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros.descricao").exists())
                .andExpect(jsonPath("$.erros.categoria").exists())
                .andExpect(jsonPath("$.erros.valor").exists())
                .andExpect(jsonPath("$.erros.competencia").exists());
    }

    @Test
    void atualizarInexistenteRetorna404() throws Exception {
        doThrow(new RecursoNaoEncontradoException("Despesa", 9L))
                .when(service).atualizar(org.mockito.ArgumentMatchers.eq(9L), org.mockito.ArgumentMatchers.any());

        mockMvc.perform(put("/api/despesas/9")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonValido()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void excluirRetorna204() throws Exception {
        mockMvc.perform(delete("/api/despesas/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void excluirInexistenteRetorna404() throws Exception {
        doThrow(new RecursoNaoEncontradoException("Despesa", 9L)).when(service).excluir(9L);

        mockMvc.perform(delete("/api/despesas/9"))
                .andExpect(status().isNotFound());
    }

    private static String jsonValido() {
        return """
                {"descricao":"Contador","categoria":"CONTADOR","valor":350.00,
                 "competencia":"2026-10","recorrente":true}
                """;
    }
}
