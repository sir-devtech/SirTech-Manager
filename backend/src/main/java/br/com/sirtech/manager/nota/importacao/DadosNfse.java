package br.com.sirtech.manager.nota.importacao;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;

/** O que a importação precisa do XML. O arquivo cru fica de fora, no caso de uso. */
public record DadosNfse(
        String numero,
        LocalDate dataEmissao,
        YearMonth competencia,
        BigDecimal valor,
        String documentoTomador,
        String documentoPrestador
) {
}
