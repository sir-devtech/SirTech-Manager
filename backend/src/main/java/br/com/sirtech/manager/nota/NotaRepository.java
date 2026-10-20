package br.com.sirtech.manager.nota;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.criteria.Predicate;

public interface NotaRepository extends JpaRepository<NotaFiscal, Long>, JpaSpecificationExecutor<NotaFiscal> {

    /**
     * Filtros opcionais montados em Java. Um {@code is null} dentro do SQL quebra no PostgreSQL
     * quando o parâmetro é uma data convertida: o banco não descobre o tipo.
     */
    default List<NotaFiscal> filtrar(YearMonth competencia, StatusNota status, Long clienteId) {
        return findAll((raiz, consulta, cb) -> {
            List<Predicate> filtros = new ArrayList<>();
            if (competencia != null) {
                filtros.add(cb.equal(raiz.get("competencia"), competencia));
            }
            if (status != null) {
                filtros.add(cb.equal(raiz.get("status"), status));
            }
            if (clienteId != null) {
                filtros.add(cb.equal(raiz.get("cliente").get("id"), clienteId));
            }
            if (consulta != null) {
                consulta.orderBy(cb.desc(raiz.get("competencia")), cb.desc(raiz.get("id")));
            }
            return cb.and(filtros.toArray(Predicate[]::new));
        });
    }

    /** Insere a nota pendente. Se já existe uma ativa naquele mês, não faz nada e devolve 0. */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = """
            INSERT INTO nota_fiscal (cliente_id, competencia, valor, data_vencimento, status)
            VALUES (:clienteId, :competencia, :valor, :vencimento, 'PENDENTE')
            ON CONFLICT (cliente_id, competencia) WHERE status <> 'CANCELADA'
            DO NOTHING
            """, nativeQuery = true)
    int inserirPendenteSeAusente(
            @Param("clienteId") Long clienteId,
            @Param("competencia") LocalDate competencia,
            @Param("valor") BigDecimal valor,
            @Param("vencimento") LocalDate vencimento);
}
