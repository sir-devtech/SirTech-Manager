package br.com.sirtech.manager.financeiro;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import br.com.sirtech.manager.cliente.Cliente;
import br.com.sirtech.manager.despesa.Despesa;
import br.com.sirtech.manager.financeiro.dto.FaixaAtraso;
import br.com.sirtech.manager.financeiro.dto.Fatia;
import br.com.sirtech.manager.financeiro.dto.FinanceiroResponse;
import br.com.sirtech.manager.financeiro.dto.MesFinanceiro;
import br.com.sirtech.manager.nota.NotaFiscal;
import br.com.sirtech.manager.nota.StatusNota;
import br.com.sirtech.manager.resumo.CalculadoraResumo;

/**
 * Contas de um intervalo de meses, sem banco e sem Spring.
 * Um mês isolado repete {@link CalculadoraResumo}. Cancelada fica fora da receita.
 */
public class CalculadoraFinanceiro {

    private static final BigDecimal ZERO = new BigDecimal("0.00");
    private static final BigDecimal METADE = new BigDecimal("0.5000");
    private final CalculadoraResumo mes = new CalculadoraResumo();

    public FinanceiroResponse calcular(
            YearMonth inicio,
            YearMonth fim,
            List<NotaFiscal> notas,
            List<Despesa> despesas,
            List<NotaFiscal> emitidasEmAberto,
            LocalDate hoje) {

        List<MesFinanceiro> meses = new ArrayList<>();
        BigDecimal prevista = ZERO;
        BigDecimal recebida = ZERO;
        BigDecimal totalDespesas = ZERO;
        Map<String, Acumulo> clientes = new LinkedHashMap<>();
        Map<String, Acumulo> produtos = new LinkedHashMap<>();
        Map<String, Acumulo> categorias = new LinkedHashMap<>();

        for (YearMonth competencia = inicio; !competencia.isAfter(fim); competencia = competencia.plusMonths(1)) {
            YearMonth mesAtual = competencia;
            List<NotaFiscal> notasDoMes = notas.stream()
                    .filter(nota -> mesAtual.equals(nota.getCompetencia()))
                    .toList();
            List<Despesa> despesasDoMes = despesas.stream()
                    .filter(despesa -> mesAtual.equals(despesa.getCompetencia()))
                    .toList();
            var resumo = mes.calcular(mesAtual, notasDoMes, despesasDoMes, hoje);
            meses.add(new MesFinanceiro(
                    mesAtual, resumo.receitaPrevista(), resumo.receitaRecebida(), resumo.totalDespesas()));
            prevista = prevista.add(resumo.receitaPrevista());
            recebida = recebida.add(resumo.receitaRecebida());
            totalDespesas = totalDespesas.add(resumo.totalDespesas());

            for (NotaFiscal nota : notasDoMes) {
                if (nota.getStatus() == StatusNota.CANCELADA) {
                    continue;
                }
                Cliente cliente = nota.getCliente();
                String chave = cliente.getId() != null ? Long.toString(cliente.getId()) : cliente.getNome();
                somar(clientes, chave, cliente.getNome(), nota.getValor());
                somar(produtos, cliente.getProduto().name(), cliente.getProduto().name(), nota.getValor());
            }
            for (Despesa despesa : despesasDoMes) {
                somar(categorias, despesa.getCategoria().name(), despesa.getCategoria().name(), despesa.getValor());
            }
        }

        return new FinanceiroResponse(
                inicio,
                fim,
                prevista,
                recebida,
                totalDespesas,
                prevista.subtract(totalDespesas),
                recebida.subtract(totalDespesas),
                meses,
                fatias(clientes, prevista),
                fatias(produtos, prevista),
                fatias(categorias, totalDespesas),
                atraso(emitidasEmAberto, hoje));
    }

    /** Verdadeiro quando um cliente sozinho passa de metade da receita prevista do período. */
    public static boolean concentrado(List<Fatia> porCliente) {
        return porCliente.stream().anyMatch(fatia -> fatia.parte().compareTo(METADE) > 0);
    }

    private static FaixaAtraso atraso(List<NotaFiscal> emitidas, LocalDate hoje) {
        int aVencer = 0;
        int ate30 = 0;
        int de31a60 = 0;
        int acima = 0;
        BigDecimal valorAVencer = ZERO;
        BigDecimal valorAte30 = ZERO;
        BigDecimal valorDe31a60 = ZERO;
        BigDecimal valorAcima = ZERO;
        for (NotaFiscal nota : emitidas) {
            long dias = ChronoUnit.DAYS.between(nota.getDataVencimento(), hoje);
            BigDecimal valor = nota.getValor();
            if (dias <= 0) {
                aVencer++;
                valorAVencer = valorAVencer.add(valor);
            } else if (dias <= 30) {
                ate30++;
                valorAte30 = valorAte30.add(valor);
            } else if (dias <= 60) {
                de31a60++;
                valorDe31a60 = valorDe31a60.add(valor);
            } else {
                acima++;
                valorAcima = valorAcima.add(valor);
            }
        }
        return new FaixaAtraso(aVencer, valorAVencer, ate30, valorAte30, de31a60, valorDe31a60, acima, valorAcima);
    }

    private static void somar(Map<String, Acumulo> grupos, String chave, String nome, BigDecimal valor) {
        grupos.computeIfAbsent(chave, ignorada -> new Acumulo(nome)).valor = grupos.get(chave).valor.add(valor);
    }

    private static List<Fatia> fatias(Map<String, Acumulo> grupos, BigDecimal total) {
        return grupos.entrySet().stream()
                .map(entrada -> new Fatia(entrada.getKey(), entrada.getValue().nome, entrada.getValue().valor, parte(entrada.getValue().valor, total)))
                .sorted(Comparator.comparing(Fatia::valor).reversed())
                .toList();
    }

    private static BigDecimal parte(BigDecimal valor, BigDecimal total) {
        if (total.signum() == 0) {
            return new BigDecimal("0.0000");
        }
        return valor.divide(total, 4, RoundingMode.HALF_UP);
    }

    private static final class Acumulo {
        private final String nome;
        private BigDecimal valor = ZERO;

        private Acumulo(String nome) {
            this.nome = nome;
        }
    }
}
