package br.com.sirtech.manager.despesa;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DespesaRepository extends JpaRepository<Despesa, Long> {

    List<Despesa> findByCompetenciaOrderByDescricaoAsc(YearMonth competencia);

    @Query("""
            select despesa from Despesa despesa
            where despesa.competencia >= :inicio and despesa.competencia <= :fim
            order by despesa.competencia, despesa.descricao
            """)
    List<Despesa> noPeriodo(@Param("inicio") YearMonth inicio, @Param("fim") YearMonth fim);

    List<Despesa> findAllByOrderByCompetenciaDescDescricaoAsc();

    @Query(value = """
            SELECT MAX(competencia) FROM despesa
            WHERE recorrente AND competencia < :competencia
            """, nativeQuery = true)
    LocalDate ultimaCompetenciaRecorrenteAntesDe(@Param("competencia") LocalDate competencia);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = """
            INSERT INTO despesa (descricao, categoria, valor, competencia, recorrente)
            SELECT descricao, categoria, valor, :destino, true
            FROM despesa
            WHERE recorrente AND competencia = :origem
            ON CONFLICT (lower(descricao), categoria, competencia) WHERE recorrente
            DO NOTHING
            """, nativeQuery = true)
    int replicarRecorrentes(@Param("origem") LocalDate origem, @Param("destino") LocalDate destino);
}
