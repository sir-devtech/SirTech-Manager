package br.com.sirtech.manager.cliente;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import br.com.sirtech.manager.cliente.dto.ClienteRequest;
import br.com.sirtech.manager.cliente.dto.ClienteResponse;
import br.com.sirtech.manager.shared.excecao.RecursoNaoEncontradoException;

/**
 * Testa só a camada web (controller + validação + exception handler), com o service mockado.
 */
@WebMvcTest(ClienteController.class)
class ClienteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ClienteService service;

    @Test
    void criarRetorna201ComLocation() throws Exception {
        when(service.criar(any(ClienteRequest.class))).thenReturn(new ClienteResponse(
                1L, "Barbearia X", "11222333000181", null, Produto.SIR_BARBER,
                new BigDecimal("199.90"), 10, true, LocalDateTime.of(2026, 10, 17, 9, 0)));

        mockMvc.perform(post("/api/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Barbearia X","documento":"11.222.333/0001-81",
                                 "produto":"SIR_BARBER","valorMensal":199.90,"diaVencimento":10}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/clientes/1"))
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void criarComDadosInvalidosRetorna400ComErrosPorCampo() throws Exception {
        mockMvc.perform(post("/api/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"","documento":"123","produto":"SIR_PET",
                                 "valorMensal":0,"diaVencimento":31}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("Content-Type", MediaType.APPLICATION_PROBLEM_JSON_VALUE))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.erros.nome").exists())
                .andExpect(jsonPath("$.erros.documento").value("CPF ou CNPJ inválido"))
                .andExpect(jsonPath("$.erros.valorMensal").exists())
                .andExpect(jsonPath("$.erros.diaVencimento").exists());
    }

    @Test
    void buscarInexistenteRetorna404() throws Exception {
        when(service.buscar(99L)).thenThrow(new RecursoNaoEncontradoException("Cliente", 99L));

        mockMvc.perform(get("/api/clientes/99"))
                .andExpect(status().isNotFound())
                .andExpect(header().string("Content-Type", MediaType.APPLICATION_PROBLEM_JSON_VALUE))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value("Cliente com id 99 não encontrado(a)"));
    }
}
