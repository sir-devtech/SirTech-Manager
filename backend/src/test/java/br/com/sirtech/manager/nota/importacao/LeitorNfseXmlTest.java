package br.com.sirtech.manager.nota.importacao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import br.com.sirtech.manager.shared.excecao.XmlNfseInvalidoException;

class LeitorNfseXmlTest {

    private final LeitorNfse leitor = new LeitorNfseXml();

    @Test
    void leAbrasfPeloNomeDaTagEIgnoraNumeroDeEndereco() throws Exception {
        String xml = new ClassPathResource("nfse/nfse-ficticia.xml").getContentAsString(StandardCharsets.UTF_8);

        DadosNfse dados = leitor.ler(xml);

        assertThat(dados.numero()).isEqualTo("123");
        assertThat(dados.dataEmissao()).isEqualTo("2026-10-05");
        assertThat(dados.competencia()).hasToString("2026-10");
        assertThat(dados.valor()).isEqualByComparingTo("250.00");
        assertThat(dados.documentoTomador()).isEqualTo("11222333000181");
        assertThat(dados.documentoPrestador()).isEqualTo("12345678000195");
    }

    @Test
    void leTagsDoPadraoNacional() {
        String xml = """
                <NFSe><infNFSe>
                  <nNFSe>77</nNFSe>
                  <dhEmi>2026-11-02T09:00:00-03:00</dhEmi>
                  <prest><CNPJ>12345678000195</CNPJ></prest>
                  <toma><CPF>529.982.247-25</CPF></toma>
                  <valores><vServ>1.234,50</vServ></valores>
                </infNFSe></NFSe>
                """;

        DadosNfse dados = leitor.ler(xml);

        assertThat(dados.numero()).isEqualTo("77");
        assertThat(dados.competencia()).hasToString("2026-11");
        assertThat(dados.valor()).isEqualByComparingTo("1234.50");
        assertThat(dados.documentoTomador()).isEqualTo("52998224725");
    }

    @Test
    void recusaArquivoComDtd() {
        String xml = """
                <?xml version="1.0"?>
                <!DOCTYPE foo [<!ENTITY xxe "segredo">]>
                <CompNfse>&xxe;</CompNfse>
                """;

        assertThatThrownBy(() -> leitor.ler(xml))
                .isInstanceOf(XmlNfseInvalidoException.class);
    }
}
