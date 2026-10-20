package br.com.sirtech.manager.resumo;

import java.time.Clock;
import java.time.LocalDate;
import java.time.YearMonth;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.sirtech.manager.despesa.DespesaRepository;
import br.com.sirtech.manager.nota.NotaRepository;
import br.com.sirtech.manager.resumo.dto.ResumoResponse;

@Service
public class ResumoService {

    private final NotaRepository notas;
    private final DespesaRepository despesas;
    private final CalculadoraResumo calculadora = new CalculadoraResumo();
    private final Clock clock;

    public ResumoService(NotaRepository notas, DespesaRepository despesas, Clock clock) {
        this.notas = notas;
        this.despesas = despesas;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public ResumoResponse resumo(YearMonth competencia) {
        return calculadora.calcular(
                competencia,
                notas.filtrar(competencia, null, null),
                despesas.findByCompetenciaOrderByDescricaoAsc(competencia),
                LocalDate.now(clock));
    }
}
