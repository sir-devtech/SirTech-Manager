package br.com.sirtech.manager.backup;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.sirtech.manager.shared.excecao.FalhaBackupException;

@ExtendWith(MockitoExtension.class)
class BackupServiceTest {

    private static final Clock CLOCK = Clock.fixed(
            Instant.parse("2026-10-17T12:00:00Z"), ZoneId.of("America/Sao_Paulo"));
    private static final String SENHA = "segredo-super";

    @TempDir
    Path diretorio;

    @Mock
    private ExecutorComando executor;

    private BackupService service;

    @BeforeEach
    void setUp() {
        service = new BackupService(
                executor,
                CLOCK,
                "jdbc:postgresql://localhost:5432/sirtech?loggerLevel=OFF",
                "sirtech",
                SENHA,
                diretorio.toString(),
                "pg_dump");
    }

    @Test
    void montaArquivoComDataHoraEComandoSemSenhaNaLinha() throws Exception {
        ArgumentCaptor<List<String>> comando = ArgumentCaptor.captor();
        ArgumentCaptor<Map<String, String>> ambiente = ArgumentCaptor.captor();
        when(executor.executar(comando.capture(), ambiente.capture())).thenAnswer(invocacao -> {
            List<String> linha = invocacao.getArgument(0);
            Path arquivo = Path.of(linha.get(linha.indexOf("--file") + 1));
            Files.writeString(arquivo, "-- dump");
            return new ResultadoComando(0, "", "");
        });

        var resposta = service.criar();

        assertThat(resposta.arquivo()).isEqualTo("sirtech-20261017-090000.sql");
        assertThat(resposta.tamanhoBytes()).isEqualTo("-- dump".length());
        assertThat(resposta.criadoEm()).hasHour(9);
        assertThat(comando.getValue()).contains("pg_dump", "-h", "localhost", "-p", "5432", "-d", "sirtech");
        assertThat(comando.getValue()).doesNotContain(SENHA);
        assertThat(ambiente.getValue()).containsEntry("PGPASSWORD", SENHA);
    }

    @Test
    void erroDoProcessoViraFalhaDeBackup() throws Exception {
        when(executor.executar(any(), any())).thenReturn(new ResultadoComando(1, "", "pg_dump: falhou"));

        assertThatThrownBy(service::criar)
                .isInstanceOf(FalhaBackupException.class)
                .hasMessageContaining("pg_dump: falhou")
                .hasMessageNotContaining(SENHA);
    }
}
