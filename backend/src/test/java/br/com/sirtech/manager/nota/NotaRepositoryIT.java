package br.com.sirtech.manager.nota;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;

import br.com.sirtech.manager.AbstractIntegrationTest;
import br.com.sirtech.manager.cliente.Cliente;
import br.com.sirtech.manager.cliente.ClienteRepository;
import br.com.sirtech.manager.cliente.Produto;

class NotaRepositoryIT extends AbstractIntegrationTest {

    @Autowired
    private NotaRepository notas;

    @Autowired
    private ClienteRepository clientes;

    @Autowired
    private JdbcTemplate jdbc;

    @AfterEach
    void limpar() {
        jdbc.update("DELETE FROM despesa");
        jdbc.update("DELETE FROM nota_fiscal");
        clientes.deleteAll();
    }

    @Test
    void duasNotasAtivasNaMesmaCompetenciaFalhamNoBanco() {
        Cliente cliente = clientes.saveAndFlush(cliente());
        notas.saveAndFlush(nota(cliente));

        assertThatThrownBy(() -> notas.saveAndFlush(nota(cliente)))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("uk_nota_cliente_competencia");
    }

    @Test
    void notaCanceladaLiberaACompetenciaParaOutra() {
        Cliente cliente = clientes.saveAndFlush(cliente());
        NotaFiscal primeira = notas.saveAndFlush(nota(cliente));
        primeira.cancelar();
        notas.saveAndFlush(primeira);

        notas.saveAndFlush(nota(cliente));
    }

    private static Cliente cliente() {
        return new Cliente("Barbearia X", "11222333000181", null, Produto.SIR_BARBER,
                new BigDecimal("199.90"), 10, LocalDateTime.now());
    }

    private static NotaFiscal nota(Cliente cliente) {
        return new NotaFiscal(cliente, YearMonth.of(2026, 10), new BigDecimal("199.90"), LocalDate.of(2026, 10, 10));
    }
}
