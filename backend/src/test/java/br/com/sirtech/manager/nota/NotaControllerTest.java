package br.com.sirtech.manager.nota;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import br.com.sirtech.manager.nota.dto.NotaResponse;
import br.com.sirtech.manager.shared.config.AnoMesWebConfig;
import br.com.sirtech.manager.shared.excecao.RecursoNaoEncontradoException;
import br.com.sirtech.manager.shared.excecao.RegraNegocioException;

// Filtros de segurança desligados: aqui o assunto é validação e formato de erro.
// O login tem teste próprio em AcessoIT.
@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(NotaController.class)
@Import(AnoMesWebConfig.class)
class NotaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NotaService service;

    @Test
    void criarComDadosInvalidosRetorna400() throws Exception {
        mockMvc.perform(post("/api/notas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros.clienteId").exists())
                .andExpect(jsonPath("$.erros.competencia").exists());
    }

    @Test
    void buscarInexistenteRetorna404() throws Exception {
        when(service.buscar(9L)).thenThrow(new RecursoNaoEncontradoException("Nota fiscal", 9L));

        mockMvc.perform(get("/api/notas/9"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void criarDeClienteInativoRetorna422() throws Exception {
        when(service.criar(any())).thenThrow(new RegraNegocioException("Cliente inativo não pode receber nota"));

        mockMvc.perform(post("/api/notas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"clienteId":1,"competencia":"2026-10"}
                                """))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.title").value("Regra de negócio violada"));
    }

    @Test
    void pagarSemDataRetorna400() throws Exception {
        mockMvc.perform(patch("/api/notas/1/pagar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros.dataPagamento").exists());
    }

    @Test
    void listarEncaminhaCompetenciaConvertida() throws Exception {
        when(service.listar(YearMonth.of(2026, 10), StatusNota.PENDENTE, 1L))
                .thenReturn(java.util.List.of(new NotaResponse(
                        3L, 1L, "Barbearia X", YearMonth.of(2026, 10), null,
                        new BigDecimal("199.90"), null, LocalDate.of(2026, 10, 10),
                        null, StatusNota.PENDENTE, false)));

        mockMvc.perform(get("/api/notas")
                        .param("competencia", "2026-10")
                        .param("status", "PENDENTE")
                        .param("clienteId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].clienteNome").value("Barbearia X"))
                .andExpect(jsonPath("$[0].vencida").value(false));
    }
}
