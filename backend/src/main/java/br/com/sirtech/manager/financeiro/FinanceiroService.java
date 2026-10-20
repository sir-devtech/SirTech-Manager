package br.com.sirtech.manager.financeiro;

import java.time.Clock;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.sirtech.manager.despesa.DespesaRepository;
import br.com.sirtech.manager.financeiro.dto.FinanceiroResponse;
import br.com.sirtech.manager.nota.NotaRepository;
import br.com.sirtech.manager.shared.excecao.RegraNegocioException;

@Service
public class FinanceiroService {

    static final int MESES_NO_MAXIMO = 36;

    private final NotaRepository notas;
    private final DespesaRepository despesas;
    private final CalculadoraFinanceiro calculadora = new CalculadoraFinanceiro();
    private final Clock clock;

    public FinanceiroService(NotaRepository notas, DespesaRepository despesas, Clock clock) {
        this.notas = notas;
        this.despesas = despesas;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public FinanceiroResponse consultar(YearMonth inicio, YearMonth fim) {
        if (inicio.isAfter(fim)) {
            throw new RegraNegocioException("O início do período não pode ser depois do fim");
        }
        long meses = inicio.until(fim, ChronoUnit.MONTHS) + 1;
        if (meses > MESES_NO_MAXIMO) {
            throw new RegraNegocioException("O período pode ter no máximo " + MESES_NO_MAXIMO + " meses");
        }
        return calculadora.calcular(
                inicio,
                fim,
                notas.noPeriodo(inicio, fim),
                despesas.noPeriodo(inicio, fim),
                notas.emitidasEmAberto(),
                LocalDate.now(clock));
    }
}
