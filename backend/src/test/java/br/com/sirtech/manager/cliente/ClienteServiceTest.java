package br.com.sirtech.manager.cliente;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.sirtech.manager.cliente.dto.ClienteRequest;
import br.com.sirtech.manager.cliente.dto.ClienteResponse;
import br.com.sirtech.manager.shared.excecao.RecursoNaoEncontradoException;

@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    private static final ZoneId SAO_PAULO = ZoneId.of("America/Sao_Paulo");
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-10-17T12:00:00Z"), SAO_PAULO);

    @Mock
    private ClienteRepository repository;

    private ClienteService service;

    @BeforeEach
    void setUp() {
        service = new ClienteService(repository, CLOCK);
    }

    @Test
    void criarSalvaDocumentoSoComDigitosEDataDoRelogio() {
        when(repository.saveAndFlush(any(Cliente.class))).thenAnswer(inv -> inv.getArgument(0));

        service.criar(new ClienteRequest("Barbearia X", "11.222.333/0001-81", null,
                Produto.SIR_BARBER, new BigDecimal("199.90"), 10));

        ArgumentCaptor<Cliente> captor = ArgumentCaptor.forClass(Cliente.class);
        verify(repository).saveAndFlush(captor.capture());
        Cliente salvo = captor.getValue();
        assertThat(salvo.getDocumento()).isEqualTo("11222333000181");
        assertThat(salvo.isAtivo()).isTrue();
        assertThat(salvo.getCriadoEm()).isEqualTo(LocalDateTime.now(CLOCK));
    }

    @Test
    void buscarInexistenteLancaRecursoNaoEncontrado() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscar(99L))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessageContaining("99");
    }

    @Test
    void desativarEDepoisAtivarMantemOHistorico() {
        Cliente cliente = new Cliente("Pet Y", "11444777000161", null, Produto.SIR_PET,
                new BigDecimal("249.90"), 15, LocalDateTime.now(CLOCK));
        when(repository.findById(1L)).thenReturn(Optional.of(cliente));

        ClienteResponse desativado = service.desativar(1L);
        assertThat(desativado.ativo()).isFalse();

        ClienteResponse ativado = service.ativar(1L);
        assertThat(ativado.ativo()).isTrue();
        assertThat(ativado.nome()).isEqualTo("Pet Y");
    }
}
