package br.com.sirtech.manager.cliente;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.sirtech.manager.cliente.dto.ClienteRequest;
import br.com.sirtech.manager.cliente.dto.ClienteResponse;
import br.com.sirtech.manager.nota.NotaService;
import br.com.sirtech.manager.shared.excecao.RecursoNaoEncontradoException;
import br.com.sirtech.manager.shared.validacao.CpfOuCnpjValidator;

@Service
public class ClienteService {

    private final ClienteRepository repository;
    private final Clock clock;
    private final NotaService notas;

    public ClienteService(ClienteRepository repository, Clock clock, @Lazy NotaService notas) {
        this.repository = repository;
        this.clock = clock;
        this.notas = notas;
    }

    @Transactional
    public ClienteResponse criar(ClienteRequest request) {
        Cliente cliente = new Cliente(
                request.nome(),
                CpfOuCnpjValidator.somenteDigitos(request.documento()),
                request.email(),
                request.produto(),
                request.valorMensal(),
                request.diaVencimento(),
                LocalDateTime.now(clock));
        // saveAndFlush força o INSERT agora: a violação de documento único estoura aqui
        // (e vira 409) em vez de só no commit da transação.
        return ClienteResponse.de(repository.saveAndFlush(cliente));
    }

    @Transactional(readOnly = true)
    public List<ClienteResponse> listar(Boolean ativo) {
        List<Cliente> clientes = ativo == null
                ? repository.findAllByOrderByNomeAsc()
                : repository.findByAtivoOrderByNomeAsc(ativo);
        return clientes.stream().map(ClienteResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public ClienteResponse buscar(Long id) {
        return ClienteResponse.de(buscarEntidade(id));
    }

    @Transactional
    public ClienteResponse atualizar(Long id, ClienteRequest request) {
        Cliente cliente = buscarEntidade(id);
        cliente.atualizar(
                request.nome(),
                CpfOuCnpjValidator.somenteDigitos(request.documento()),
                request.email(),
                request.produto(),
                request.valorMensal(),
                request.diaVencimento());
        repository.flush();
        notas.realinharPendentes(cliente);
        return ClienteResponse.de(cliente);
    }

    @Transactional
    public ClienteResponse ativar(Long id) {
        Cliente cliente = buscarEntidade(id);
        cliente.ativar();
        return ClienteResponse.de(cliente);
    }

    @Transactional
    public ClienteResponse desativar(Long id) {
        Cliente cliente = buscarEntidade(id);
        cliente.desativar();
        return ClienteResponse.de(cliente);
    }

    /** Usado também por outros módulos (ex.: notas) que precisam da entidade. */
    @Transactional(readOnly = true)
    public Cliente buscarEntidade(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente", id));
    }

    /** Outros módulos procuram o cliente pelo documento sem abrir o repositório dele. */
    @Transactional(readOnly = true)
    public Optional<Cliente> procurarPorDocumento(String documento) {
        return repository.findByDocumento(CpfOuCnpjValidator.somenteDigitos(documento));
    }
}
