package br.com.sirtech.manager.geracao;

/** Quantas linhas novas a geração realmente gravou. Zero significa "já estava lá". */
public record ResultadoGeracao(int notasCriadas, int despesasReplicadas) {
}
