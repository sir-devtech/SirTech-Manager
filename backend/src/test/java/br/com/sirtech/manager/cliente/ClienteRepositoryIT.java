package br.com.sirtech.manager.cliente;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;

import br.com.sirtech.manager.AbstractIntegrationTest;

class ClienteRepositoryIT extends AbstractIntegrationTest {

    @Autowired
    private ClienteRepository repository;

    @Autowired
    private JdbcTemplate jdbc;

    @AfterEach
    void limpar() {
        jdbc.update("DELETE FROM despesa");
        jdbc.update("DELETE FROM nota_fiscal");
        repository.deleteAll();
    }

    @Test
    void bancoRejeitaDocumentoDuplicado() {
        repository.saveAndFlush(cliente("11222333000181"));

        assertThatThrownBy(() -> repository.saveAndFlush(cliente("11222333000181")))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("uk_cliente_documento");
    }

    @Test
    void bancoRejeitaDiaDeVencimentoForaDaFaixa() {
        Cliente invalido = new Cliente("X", "52998224725", null, Produto.OUTRO,
                BigDecimal.TEN, 31, LocalDateTime.now());

        assertThatThrownBy(() -> repository.saveAndFlush(invalido))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("ck_cliente_dia_vencimento");
    }

    private static Cliente cliente(String documento) {
        return new Cliente("Cliente " + documento, documento, null, Produto.SIR_BARBER,
                new BigDecimal("199.90"), 10, LocalDateTime.now());
    }
}
