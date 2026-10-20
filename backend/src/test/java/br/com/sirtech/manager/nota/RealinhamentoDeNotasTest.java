package br.com.sirtech.manager.nota;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import br.com.sirtech.manager.cliente.Cliente;
import br.com.sirtech.manager.cliente.ClienteAlterado;
import br.com.sirtech.manager.cliente.Produto;

class RealinhamentoDeNotasTest {

    @Test
    void clienteAlteradoRealinhaAsNotasPendentesDele() {
        NotaService notas = mock(NotaService.class);
        Cliente cliente = new Cliente("Pet Y", "11444777000161", null, Produto.SIR_PET,
                new BigDecimal("280.00"), 5, LocalDateTime.of(2026, 10, 1, 9, 0));

        new RealinhamentoDeNotas(notas).aoAlterarCliente(new ClienteAlterado(cliente));

        verify(notas).realinharPendentes(cliente);
    }
}
