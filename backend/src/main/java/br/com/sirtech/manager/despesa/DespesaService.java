package br.com.sirtech.manager.despesa;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.sirtech.manager.despesa.dto.DespesaRequest;
import br.com.sirtech.manager.despesa.dto.DespesaResponse;
import br.com.sirtech.manager.shared.excecao.RecursoNaoEncontradoException;

@Service
public class DespesaService {

    private final DespesaRepository repository;

    public DespesaService(DespesaRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public DespesaResponse criar(DespesaRequest request) {
        Despesa despesa = new Despesa(
                request.descricao(),
                request.categoria(),
                request.valor(),
                request.competencia(),
                request.recorrente());
        return DespesaResponse.de(repository.saveAndFlush(despesa));
    }

    @Transactional(readOnly = true)
    public List<DespesaResponse> listar(YearMonth competencia) {
        List<Despesa> despesas = competencia == null
                ? repository.findAllByOrderByCompetenciaDescDescricaoAsc()
                : repository.findByCompetenciaOrderByDescricaoAsc(competencia);
        return despesas.stream().map(DespesaResponse::de).toList();
    }

    @Transactional
    public DespesaResponse atualizar(Long id, DespesaRequest request) {
        Despesa despesa = buscar(id);
        despesa.atualizar(
                request.descricao(),
                request.categoria(),
                request.valor(),
                request.competencia(),
                request.recorrente());
        repository.flush();
        return DespesaResponse.de(despesa);
    }

    @Transactional
    public void excluir(Long id) {
        repository.delete(buscar(id));
    }

    /**
     * Copia as recorrentes da última competência anterior que tenha alguma.
     * Pular um mês não quebra a cadeia. Devolve quantas linhas novas entraram.
     */
    @Transactional
    public int replicarRecorrentes(YearMonth destino) {
        LocalDate origem = repository.ultimaCompetenciaRecorrenteAntesDe(destino.atDay(1));
        if (origem == null) {
            return 0;
        }
        return repository.replicarRecorrentes(origem, destino.atDay(1));
    }

    private Despesa buscar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Despesa", id));
    }
}
