package br.com.sirtech.manager.nota;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.sirtech.manager.cliente.Cliente;
import br.com.sirtech.manager.cliente.ClienteService;
import br.com.sirtech.manager.nota.dto.EmitirNotaRequest;
import br.com.sirtech.manager.nota.dto.NotaRequest;
import br.com.sirtech.manager.nota.dto.NotaResponse;
import br.com.sirtech.manager.nota.dto.PagarNotaRequest;
import br.com.sirtech.manager.shared.excecao.RecursoNaoEncontradoException;
import br.com.sirtech.manager.shared.excecao.RegraNegocioException;

@Service
public class NotaService {

    private final NotaRepository repository;
    private final ClienteService clienteService;
    private final Clock clock;

    public NotaService(NotaRepository repository, ClienteService clienteService, Clock clock) {
        this.repository = repository;
        this.clienteService = clienteService;
        this.clock = clock;
    }

    @Transactional
    public NotaResponse criar(NotaRequest request) {
        Cliente cliente = clienteService.buscarEntidade(request.clienteId());
        if (!cliente.isAtivo()) {
            throw new RegraNegocioException("Cliente inativo não pode receber nota");
        }
        var valor = request.valor() != null ? request.valor() : cliente.getValorMensal();
        var vencimento = request.competencia().atDay(cliente.getDiaVencimento());
        NotaFiscal nota = new NotaFiscal(cliente, request.competencia(), valor, vencimento);
        return NotaResponse.de(repository.saveAndFlush(nota), hoje());
    }

    @Transactional(readOnly = true)
    public List<NotaResponse> listar(YearMonth competencia, StatusNota status, Long clienteId) {
        return repository.filtrar(competencia, status, clienteId).stream()
                .map(nota -> NotaResponse.de(nota, hoje()))
                .toList();
    }

    @Transactional(readOnly = true)
    public NotaResponse buscar(Long id) {
        return NotaResponse.de(buscarEntidade(id), hoje());
    }

    @Transactional
    public NotaResponse emitir(Long id, EmitirNotaRequest request) {
        NotaFiscal nota = buscarEntidade(id);
        nota.emitir(request.numero(), request.dataEmissao());
        return NotaResponse.de(nota, hoje());
    }

    @Transactional
    public NotaResponse pagar(Long id, PagarNotaRequest request) {
        NotaFiscal nota = buscarEntidade(id);
        nota.pagar(request.dataPagamento());
        return NotaResponse.de(nota, hoje());
    }

    @Transactional
    public NotaResponse cancelar(Long id) {
        NotaFiscal nota = buscarEntidade(id);
        nota.cancelar();
        return NotaResponse.de(nota, hoje());
    }

    /** Usado pela geração mensal. Devolve 1 se criou a nota, 0 se o mês já tinha uma ativa. */
    @Transactional
    public int inserirPendenteSeAusente(Long clienteId, YearMonth competencia, BigDecimal valor, int diaVencimento) {
        return repository.inserirPendenteSeAusente(
                clienteId,
                competencia.atDay(1),
                valor,
                competencia.atDay(diaVencimento));
    }

    /**
     * Nota pendente é previsão: valor e vencimento seguem o cliente.
     * Emitida, paga ou cancelada não muda.
     */
    @Transactional
    public void realinharPendentes(Cliente cliente) {
        repository.filtrar(null, StatusNota.PENDENTE, cliente.getId()).forEach(nota -> {
            nota.ajustarValor(cliente.getValorMensal());
            nota.ajustarVencimento(nota.getCompetencia().atDay(cliente.getDiaVencimento()));
        });
    }

    private NotaFiscal buscarEntidade(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Nota fiscal", id));
    }

    private LocalDate hoje() {
        return LocalDate.now(clock);
    }
}
