package br.com.sirtech.manager.geracao;

import java.time.YearMonth;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.sirtech.manager.cliente.ClienteService;
import br.com.sirtech.manager.cliente.dto.ClienteResponse;
import br.com.sirtech.manager.despesa.DespesaService;
import br.com.sirtech.manager.nota.NotaService;

/**
 * Fachada da rotina do mês: cria a nota pendente de cada cliente ativo e copia
 * os gastos recorrentes. Rodar de novo não duplica, porque quem garante é o índice do banco.
 */
@Service
public class GeracaoMensalService {

    private final ClienteService clientes;
    private final NotaService notas;
    private final DespesaService despesas;

    public GeracaoMensalService(ClienteService clientes, NotaService notas, DespesaService despesas) {
        this.clientes = clientes;
        this.notas = notas;
        this.despesas = despesas;
    }

    @Transactional
    public ResultadoGeracao gerar(YearMonth competencia) {
        int notasCriadas = 0;
        for (ClienteResponse cliente : clientes.listar(true)) {
            if (!cliente.ativo()) {
                continue;
            }
            notasCriadas += notas.inserirPendenteSeAusente(
                    cliente.id(), competencia, cliente.valorMensal(), cliente.diaVencimento());
        }
        int despesasReplicadas = despesas.replicarRecorrentes(competencia);
        return new ResultadoGeracao(notasCriadas, despesasReplicadas);
    }
}
