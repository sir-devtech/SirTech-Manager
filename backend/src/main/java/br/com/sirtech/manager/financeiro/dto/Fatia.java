package br.com.sirtech.manager.financeiro.dto;

import java.math.BigDecimal;

/** Uma fatia do período. `parte` é a fração de 0 a 1 sobre o total daquele grupo. */
public record Fatia(String chave, String nome, BigDecimal valor, BigDecimal parte) {
}
