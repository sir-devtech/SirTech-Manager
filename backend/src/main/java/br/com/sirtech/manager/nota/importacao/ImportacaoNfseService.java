package br.com.sirtech.manager.nota.importacao;

import java.time.Clock;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.sirtech.manager.cliente.Cliente;
import br.com.sirtech.manager.cliente.ClienteService;
import br.com.sirtech.manager.nota.NotaFiscal;
import br.com.sirtech.manager.nota.NotaRepository;
import br.com.sirtech.manager.nota.StatusNota;
import br.com.sirtech.manager.nota.dto.NotaResponse;
import br.com.sirtech.manager.shared.excecao.ConflitoException;
import br.com.sirtech.manager.shared.excecao.RegraNegocioException;
import br.com.sirtech.manager.shared.validacao.CpfOuCnpjValidator;

/**
 * Caso de uso: lê o XML, acha o cliente e deixa a nota emitida.
 * Não fala com a prefeitura. Uma transação só.
 */
@Service
public class ImportacaoNfseService {

    private final LeitorNfse leitor;
    private final ClienteService clientes;
    private final NotaRepository notas;
    private final Clock clock;
    private final String cnpjSirtech;

    public ImportacaoNfseService(
            LeitorNfse leitor,
            ClienteService clientes,
            NotaRepository notas,
            Clock clock,
            @Value("${sirtech.cnpj:}") String cnpjSirtech) {
        this.leitor = leitor;
        this.clientes = clientes;
        this.notas = notas;
        this.clock = clock;
        this.cnpjSirtech = cnpjSirtech == null ? "" : cnpjSirtech;
    }

    @Transactional
    public NotaResponse importar(String xml) {
        DadosNfse dados = leitor.ler(xml);
        conferirPrestador(dados.documentoPrestador());
        Cliente cliente = clienteAtivo(dados.documentoTomador());

        NotaFiscal nota = notaAtiva(cliente.getId(), dados.competencia())
                .map(atual -> aplicarNaExistente(atual, dados, xml))
                .orElseGet(() -> novaEmitida(cliente, dados, xml));
        return NotaResponse.de(notas.saveAndFlush(nota), LocalDate.now(clock));
    }

    private void conferirPrestador(String documentoPrestador) {
        String esperado = CpfOuCnpjValidator.somenteDigitos(cnpjSirtech);
        if (esperado.isBlank() || esperado.equals(documentoPrestador)) {
            return;
        }
        throw new RegraNegocioException("Esta NFS-e não foi emitida pela Sir Tech");
    }

    private Cliente clienteAtivo(String documento) {
        Cliente cliente = clientes.procurarPorDocumento(documento)
                .orElseThrow(() -> new RegraNegocioException(
                        "Nenhum cliente cadastrado com este CPF/CNPJ. Cadastre o cliente antes de importar a nota"));
        if (!cliente.isAtivo()) {
            throw new RegraNegocioException("O cliente desta NFS-e está inativo");
        }
        return cliente;
    }

    private Optional<NotaFiscal> notaAtiva(Long clienteId, YearMonth competencia) {
        return notas.filtrar(competencia, null, clienteId).stream()
                .filter(nota -> nota.getStatus() != StatusNota.CANCELADA)
                .findFirst();
    }

    private NotaFiscal aplicarNaExistente(NotaFiscal nota, DadosNfse dados, String xml) {
        if (nota.getNumero() != null && !nota.getNumero().equals(dados.numero())) {
            throw new ConflitoException(
                    "Já existe uma nota ativa deste cliente nesta competência com outro número");
        }
        if (nota.getStatus() == StatusNota.PENDENTE) {
            if (nota.getValor().compareTo(dados.valor()) != 0) {
                nota.ajustarValor(dados.valor());
            }
            nota.emitir(dados.numero(), dados.dataEmissao());
        }
        nota.guardarXml(xml);
        return nota;
    }

    private NotaFiscal novaEmitida(Cliente cliente, DadosNfse dados, String xml) {
        LocalDate vencimento = dados.competencia().atDay(cliente.getDiaVencimento());
        NotaFiscal nota = new NotaFiscal(cliente, dados.competencia(), dados.valor(), vencimento);
        nota.emitir(dados.numero(), dados.dataEmissao());
        nota.guardarXml(xml);
        return nota;
    }
}
