package br.com.sirtech.manager.backup;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import br.com.sirtech.manager.backup.dto.BackupResponse;
import br.com.sirtech.manager.shared.excecao.FalhaBackupException;

@Service
public class BackupService {

    private static final DateTimeFormatter NOME = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    private final ExecutorComando executor;
    private final Clock clock;
    private final String jdbcUrl;
    private final String usuario;
    private final String senha;
    private final Path diretorio;
    private final String comando;

    public BackupService(
            ExecutorComando executor,
            Clock clock,
            @Value("${spring.datasource.url}") String jdbcUrl,
            @Value("${spring.datasource.username}") String usuario,
            @Value("${spring.datasource.password}") String senha,
            @Value("${sirtech.backup.diretorio}") String diretorio,
            @Value("${sirtech.backup.comando:pg_dump}") String comando) {
        this.executor = executor;
        this.clock = clock;
        this.jdbcUrl = jdbcUrl;
        this.usuario = usuario;
        this.senha = senha;
        this.diretorio = Path.of(diretorio);
        this.comando = comando;
    }

    public BackupResponse criar() {
        try {
            Files.createDirectories(diretorio);
            LocalDateTime agora = LocalDateTime.now(clock);
            String nome = "sirtech-" + agora.format(NOME) + ".sql";
            Path arquivo = diretorio.resolve(nome).toAbsolutePath();
            Conexao conexao = interpretar(jdbcUrl);

            List<String> linha = List.of(
                    comando,
                    "-h", conexao.host(),
                    "-p", Integer.toString(conexao.porta()),
                    "-U", usuario,
                    "-d", conexao.banco(),
                    "--format=plain",
                    "--file", arquivo.toString());

            ResultadoComando resultado = executor.executar(linha, Map.of("PGPASSWORD", senha));
            if (resultado.codigoSaida() != 0) {
                throw new FalhaBackupException(mensagemDeFalha(resultado.erro()));
            }
            long tamanho = Files.exists(arquivo) ? Files.size(arquivo) : 0;
            return new BackupResponse(nome, tamanho, agora);
        } catch (FalhaBackupException excecao) {
            throw excecao;
        } catch (InterruptedException excecao) {
            Thread.currentThread().interrupt();
            throw new FalhaBackupException("O backup foi interrompido");
        } catch (IOException excecao) {
            throw new FalhaBackupException("Não foi possível gravar o backup");
        }
    }

    static Conexao interpretar(String jdbcUrl) {
        URI uri = URI.create(jdbcUrl.substring("jdbc:".length()));
        int porta = uri.getPort() == -1 ? 5432 : uri.getPort();
        String banco = uri.getPath() == null ? "" : uri.getPath().replaceFirst("^/", "");
        return new Conexao(uri.getHost(), porta, banco);
    }

    private static String mensagemDeFalha(String erro) {
        String texto = erro == null || erro.isBlank() ? "pg_dump terminou com erro" : erro.strip();
        return texto.length() > 500 ? texto.substring(0, 500) : texto;
    }

    record Conexao(String host, int porta, String banco) {
    }
}
