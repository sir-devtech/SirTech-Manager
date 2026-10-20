package br.com.sirtech.manager.nota;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import br.com.sirtech.manager.cliente.ClienteAlterado;

/**
 * Quando o cliente muda, as notas pendentes dele acompanham o novo valor e o novo dia.
 *
 * <p>{@link EventListener} comum é síncrono: roda na mesma thread e na mesma transação de quem
 * publicou. Se o realinhamento falhar, a edição do cliente também volta atrás.
 */
@Component
public class RealinhamentoDeNotas {

    private final NotaService notas;

    public RealinhamentoDeNotas(NotaService notas) {
        this.notas = notas;
    }

    @EventListener
    public void aoAlterarCliente(ClienteAlterado evento) {
        notas.realinharPendentes(evento.cliente());
    }
}
