package br.com.sirtech.manager.backup;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/** Roda um programa do sistema operacional. O teste troca por um dublê e não chama o pg_dump. */
public interface ExecutorComando {

    ResultadoComando executar(List<String> comando, Map<String, String> ambiente) throws IOException, InterruptedException;
}
