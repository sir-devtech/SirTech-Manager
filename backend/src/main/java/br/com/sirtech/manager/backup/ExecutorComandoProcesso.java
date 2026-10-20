package br.com.sirtech.manager.backup;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

/** Adapter: esconde o ProcessBuilder atrás de {@link ExecutorComando}. */
@Component
public class ExecutorComandoProcesso implements ExecutorComando {

    @Override
    public ResultadoComando executar(List<String> comando, Map<String, String> ambiente)
            throws IOException, InterruptedException {
        ProcessBuilder construtor = new ProcessBuilder(comando);
        construtor.environment().putAll(ambiente);
        Process processo = construtor.start();
        // Lê as duas saídas ao mesmo tempo. Se ler uma só, o processo pode travar com o buffer cheio.
        StringBuilder erro = new StringBuilder();
        Thread drenarErro = Thread.startVirtualThread(() -> {
            try {
                erro.append(new String(processo.getErrorStream().readAllBytes(), StandardCharsets.UTF_8));
            } catch (IOException excecao) {
                erro.append(excecao.getMessage());
            }
        });
        String saida = new String(processo.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        int codigo = processo.waitFor();
        drenarErro.join();
        return new ResultadoComando(codigo, saida, erro.toString());
    }
}
