package br.com.sirtech.manager.acesso;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import br.com.sirtech.manager.shared.excecao.MuitasTentativasException;

/**
 * Trava o e-mail depois de {@value #MAXIMO_DE_FALHAS} senhas erradas seguidas, por 15 minutos.
 * Fica em memória: com um usuário e um processo só, não precisa de banco nem de Redis (YAGNI).
 */
@Component
public class LimiteTentativas {

    static final int MAXIMO_DE_FALHAS = 5;
    static final Duration BLOQUEIO = Duration.ofMinutes(15);

    private record Situacao(int falhas, Instant bloqueadoAte) {
    }

    private final Map<String, Situacao> porEmail = new ConcurrentHashMap<>();
    private final Clock clock;

    public LimiteTentativas(Clock clock) {
        this.clock = clock;
    }

    /** Lança {@link MuitasTentativasException} se o e-mail ainda estiver bloqueado. */
    public void verificar(String email) {
        Situacao situacao = porEmail.get(email);
        if (situacao == null || situacao.bloqueadoAte() == null) {
            return;
        }
        Instant agora = clock.instant();
        if (agora.isBefore(situacao.bloqueadoAte())) {
            long minutos = Math.max(1, Duration.between(agora, situacao.bloqueadoAte()).toMinutes());
            throw new MuitasTentativasException(
                    "Muitas tentativas com senha errada. Tente de novo em %d minuto(s).".formatted(minutos));
        }
        porEmail.remove(email);
    }

    public void registrarFalha(String email) {
        porEmail.merge(email, new Situacao(1, null), (atual, nova) -> {
            int falhas = atual.falhas() + 1;
            Instant bloqueio = falhas >= MAXIMO_DE_FALHAS ? clock.instant().plus(BLOQUEIO) : null;
            return new Situacao(falhas, bloqueio);
        });
    }

    public void limpar(String email) {
        porEmail.remove(email);
    }
}
