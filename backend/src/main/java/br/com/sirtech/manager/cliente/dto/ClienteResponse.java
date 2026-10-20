package br.com.sirtech.manager.cliente.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import br.com.sirtech.manager.cliente.Cliente;
import br.com.sirtech.manager.cliente.Produto;

public record ClienteResponse(
        Long id,
        String nome,
        String documento,
        String email,
        Produto produto,
        BigDecimal valorMensal,
        Integer diaVencimento,
        boolean ativo,
        LocalDateTime criadoEm) {

    public static ClienteResponse de(Cliente cliente) {
        return new ClienteResponse(
                cliente.getId(),
                cliente.getNome(),
                cliente.getDocumento(),
                cliente.getEmail(),
                cliente.getProduto(),
                cliente.getValorMensal(),
                cliente.getDiaVencimento(),
                cliente.isAtivo(),
                cliente.getCriadoEm());
    }
}
