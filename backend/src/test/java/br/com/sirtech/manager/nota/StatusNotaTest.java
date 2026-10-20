package br.com.sirtech.manager.nota;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class StatusNotaTest {

    @ParameterizedTest
    @MethodSource("matriz")
    void soAsPassagensPrevistasSaoPermitidas(StatusNota origem, StatusNota destino, boolean permitida) {
        assertThat(origem.podeTransicionarPara(destino)).isEqualTo(permitida);
    }

    static Stream<Arguments> matriz() {
        return Stream.of(
                arguments(StatusNota.PENDENTE, StatusNota.PENDENTE, false),
                arguments(StatusNota.PENDENTE, StatusNota.EMITIDA, true),
                arguments(StatusNota.PENDENTE, StatusNota.PAGA, false),
                arguments(StatusNota.PENDENTE, StatusNota.CANCELADA, true),
                arguments(StatusNota.EMITIDA, StatusNota.PENDENTE, false),
                arguments(StatusNota.EMITIDA, StatusNota.EMITIDA, false),
                arguments(StatusNota.EMITIDA, StatusNota.PAGA, true),
                arguments(StatusNota.EMITIDA, StatusNota.CANCELADA, true),
                arguments(StatusNota.PAGA, StatusNota.PENDENTE, false),
                arguments(StatusNota.PAGA, StatusNota.EMITIDA, false),
                arguments(StatusNota.PAGA, StatusNota.PAGA, false),
                arguments(StatusNota.PAGA, StatusNota.CANCELADA, false),
                arguments(StatusNota.CANCELADA, StatusNota.PENDENTE, false),
                arguments(StatusNota.CANCELADA, StatusNota.EMITIDA, false),
                arguments(StatusNota.CANCELADA, StatusNota.PAGA, false),
                arguments(StatusNota.CANCELADA, StatusNota.CANCELADA, false));
    }
}
