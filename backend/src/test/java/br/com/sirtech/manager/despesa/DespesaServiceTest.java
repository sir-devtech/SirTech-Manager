package br.com.sirtech.manager.despesa;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.sirtech.manager.despesa.dto.DespesaRequest;
import br.com.sirtech.manager.shared.excecao.RecursoNaoEncontradoException;

@ExtendWith(MockitoExtension.class)
class DespesaServiceTest {

    @Mock
    private DespesaRepository repository;

    private DespesaService service;

    @BeforeEach
    void setUp() {
        service = new DespesaService(repository);
    }

    @Test
    void criarGuardaRecorrenteECompetencia() {
        when(repository.saveAndFlush(any(Despesa.class))).thenAnswer(inv -> inv.getArgument(0));

        service.criar(pedido(true));

        ArgumentCaptor<Despesa> captor = ArgumentCaptor.forClass(Despesa.class);
        verify(repository).saveAndFlush(captor.capture());
        Despesa salva = captor.getValue();
        assertThat(salva.getDescricao()).isEqualTo("Contador");
        assertThat(salva.getCategoria()).isEqualTo(CategoriaDespesa.CONTADOR);
        assertThat(salva.getValor()).isEqualByComparingTo("350.00");
        assertThat(salva.getCompetencia()).isEqualTo(YearMonth.of(2026, 10));
        assertThat(salva.isRecorrente()).isTrue();
    }

    @Test
    void atualizarInexistenteLanca404() {
        when(repository.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.atualizar(9L, pedido(false)))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessageContaining("9");
    }

    @Test
    void excluirInexistenteLanca404() {
        when(repository.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.excluir(9L))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }

    private static DespesaRequest pedido(boolean recorrente) {
        return new DespesaRequest("Contador", CategoriaDespesa.CONTADOR,
                new BigDecimal("350.00"), YearMonth.of(2026, 10), recorrente);
    }
}
