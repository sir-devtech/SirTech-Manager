package br.com.sirtech.manager.backup;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import br.com.sirtech.manager.backup.dto.BackupResponse;
import br.com.sirtech.manager.shared.excecao.FalhaBackupException;

@WebMvcTest(BackupController.class)
class BackupControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BackupService service;

    @Test
    void criarRetorna201() throws Exception {
        when(service.criar()).thenReturn(new BackupResponse(
                "sirtech-20261017-090000.sql", 12, LocalDateTime.of(2026, 10, 17, 9, 0)));

        mockMvc.perform(post("/api/backup"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.arquivo").value("sirtech-20261017-090000.sql"))
                .andExpect(jsonPath("$.tamanhoBytes").value(12));
    }

    @Test
    void falhaDoProcessoRetorna500() throws Exception {
        when(service.criar()).thenThrow(new FalhaBackupException("pg_dump: falhou"));

        mockMvc.perform(post("/api/backup"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.title").value("Falha no backup"))
                .andExpect(jsonPath("$.detail").value("pg_dump: falhou"));
    }
}
