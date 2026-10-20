package br.com.sirtech.manager.acesso;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;

import br.com.sirtech.manager.shared.excecao.MuitasTentativasException;

class LimiteTentativasTest {

    private static final String EMAIL = "dona@sirtech.example";

    /** Relógio que o teste avança na mão, para provar o fim do bloqueio sem esperar 15 minutos. */
    private static final class RelogioManual extends Clock {
        private Instant agora = Instant.parse("2026-10-20T12:00:00Z");

        void avancar(Duration tempo) {
            agora = agora.plus(tempo);
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return agora;
        }
    }

    private final RelogioManual relogio = new RelogioManual();
    private final LimiteTentativas limite = new LimiteTentativas(relogio);

    @Test
    void quatroErrosAindaDeixamTentar() {
        errar(LimiteTentativas.MAXIMO_DE_FALHAS - 1);

        assertThatCode(() -> limite.verificar(EMAIL)).doesNotThrowAnyException();
    }

    @Test
    void quintoErroBloqueiaPorQuinzeMinutos() {
        errar(LimiteTentativas.MAXIMO_DE_FALHAS);

        assertThatThrownBy(() -> limite.verificar(EMAIL))
                .isInstanceOf(MuitasTentativasException.class)
                .hasMessageContaining("15 minuto");

        relogio.avancar(LimiteTentativas.BLOQUEIO.plusSeconds(1));
        assertThatCode(() -> limite.verificar(EMAIL)).doesNotThrowAnyException();
    }

    @Test
    void acertoZeraOsErrosEOutroEmailNaoEhAfetado() {
        errar(LimiteTentativas.MAXIMO_DE_FALHAS - 1);
        limite.limpar(EMAIL);
        errar(LimiteTentativas.MAXIMO_DE_FALHAS - 1);

        assertThatCode(() -> limite.verificar(EMAIL)).doesNotThrowAnyException();
        assertThatCode(() -> limite.verificar("outra@sirtech.example")).doesNotThrowAnyException();
    }

    private void errar(int vezes) {
        for (int i = 0; i < vezes; i++) {
            limite.registrarFalha(EMAIL);
        }
    }
}
