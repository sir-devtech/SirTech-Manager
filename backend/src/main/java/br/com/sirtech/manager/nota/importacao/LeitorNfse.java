package br.com.sirtech.manager.nota.importacao;

/**
 * Porta do leitor. O caso de uso não conhece DOM, namespace nem o layout da prefeitura.
 * Um segundo padrão vira outra classe que implementa esta interface.
 */
public interface LeitorNfse {

    DadosNfse ler(String xml);
}
