package br.com.sirtech.manager.nota.importacao;

import java.io.StringReader;
import java.math.BigDecimal;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Set;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;

import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import br.com.sirtech.manager.shared.excecao.XmlNfseInvalidoException;
import br.com.sirtech.manager.shared.validacao.CpfOuCnpjValidator;

/**
 * Lê NFS-e pelo nome da tag, ignorando o namespace da prefeitura.
 * Cobre o padrão ABRASF (Numero, DataEmissao, ValorServicos) e o nacional (nNFSe, dhEmi, vServ).
 */
@Component
public class LeitorNfseXml implements LeitorNfse {

    private static final Set<String> FORA_DO_CABECALHO = Set.of(
            "TomadorServico", "toma", "Tomador",
            "PrestadorServico", "prest", "Prestador",
            "Endereco", "end");

    @Override
    public DadosNfse ler(String xml) {
        if (xml == null || xml.isBlank()) {
            throw new XmlNfseInvalidoException("O arquivo XML está vazio");
        }
        Element raiz = documento(xml).getDocumentElement();
        Element nota = primeiro(raiz, "InfNfse", "infNFSe", "infDPS");
        Element escopo = nota != null ? nota : raiz;

        String numero = textoForaDe(escopo, "nNFSe", "Numero");
        LocalDate emissao = data(textoForaDe(escopo, "dhEmi", "DataEmissao"));
        YearMonth competencia = competencia(textoForaDe(escopo, "dCompet", "Competencia"), emissao);
        BigDecimal valor = dinheiro(textoForaDe(escopo, "ValorServicos", "vServ"));
        String tomador = documentoDe(primeiro(escopo, "TomadorServico", "toma", "Tomador"));
        String prestador = documentoDe(primeiro(escopo, "PrestadorServico", "prest", "Prestador"));

        exigir(numero, "A NFS-e não tem número");
        exigir(tomador, "A NFS-e não tem CPF/CNPJ do tomador");
        return new DadosNfse(numero, emissao, competencia, valor, tomador, prestador);
    }

    private static Document documento(String xml) {
        try {
            DocumentBuilderFactory fabrica = DocumentBuilderFactory.newInstance();
            fabrica.setNamespaceAware(true);
            fabrica.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            fabrica.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            fabrica.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            fabrica.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
            Document documento = fabrica.newDocumentBuilder().parse(new InputSource(new StringReader(xml)));
            documento.getDocumentElement().normalize();
            return documento;
        } catch (Exception ex) {
            throw new XmlNfseInvalidoException("O arquivo não é um XML de NFS-e válido");
        }
    }

    private static String textoForaDe(Element escopo, String... nomes) {
        NodeList lista = escopo.getElementsByTagName("*");
        for (int i = 0; i < lista.getLength(); i++) {
            Element elemento = (Element) lista.item(i);
            if (!nomeBate(elemento, nomes) || temAncestral(elemento, FORA_DO_CABECALHO)) {
                continue;
            }
            String texto = elemento.getTextContent();
            if (texto != null && !texto.isBlank()) {
                return texto.trim();
            }
        }
        return null;
    }

    private static Element primeiro(Element escopo, String... nomes) {
        NodeList lista = escopo.getElementsByTagName("*");
        for (int i = 0; i < lista.getLength(); i++) {
            Element elemento = (Element) lista.item(i);
            if (nomeBate(elemento, nomes)) {
                return elemento;
            }
        }
        return null;
    }

    private static String documentoDe(Element parte) {
        if (parte == null) {
            return null;
        }
        NodeList lista = parte.getElementsByTagName("*");
        for (int i = 0; i < lista.getLength(); i++) {
            Element elemento = (Element) lista.item(i);
            if (!nomeBate(elemento, "Cnpj", "CNPJ", "Cpf", "CPF")) {
                continue;
            }
            String digitos = CpfOuCnpjValidator.somenteDigitos(elemento.getTextContent());
            if (!digitos.isBlank()) {
                return digitos;
            }
        }
        return null;
    }

    private static boolean nomeBate(Element elemento, String... nomes) {
        String local = elemento.getLocalName() != null ? elemento.getLocalName() : elemento.getNodeName();
        for (String nome : nomes) {
            if (nome.equals(local)) {
                return true;
            }
        }
        return false;
    }

    private static boolean temAncestral(Element elemento, Set<String> nomes) {
        Node pai = elemento.getParentNode();
        while (pai instanceof Element ancestral) {
            if (nomeBate(ancestral, nomes.toArray(String[]::new))) {
                return true;
            }
            pai = ancestral.getParentNode();
        }
        return false;
    }

    private static LocalDate data(String texto) {
        if (texto == null || texto.length() < 10) {
            throw new XmlNfseInvalidoException("A NFS-e não tem data de emissão");
        }
        try {
            return LocalDate.parse(texto.substring(0, 10));
        } catch (DateTimeException ex) {
            throw new XmlNfseInvalidoException("A data de emissão da NFS-e é inválida");
        }
    }

    private static YearMonth competencia(String texto, LocalDate emissao) {
        if (texto != null && texto.length() >= 7) {
            try {
                return YearMonth.parse(texto.substring(0, 7));
            } catch (DateTimeException ex) {
                throw new XmlNfseInvalidoException("A competência da NFS-e é inválida");
            }
        }
        return YearMonth.from(emissao);
    }

    private static BigDecimal dinheiro(String texto) {
        if (texto == null || texto.isBlank()) {
            throw new XmlNfseInvalidoException("A NFS-e não tem valor do serviço");
        }
        String limpo = texto.trim().replace(" ", "");
        if (limpo.contains(",")) {
            limpo = limpo.replace(".", "").replace(",", ".");
        }
        try {
            BigDecimal valor = new BigDecimal(limpo);
            if (valor.signum() <= 0) {
                throw new XmlNfseInvalidoException("O valor do serviço precisa ser maior que zero");
            }
            return valor;
        } catch (NumberFormatException ex) {
            throw new XmlNfseInvalidoException("O valor do serviço da NFS-e é inválido");
        }
    }

    private static void exigir(String valor, String mensagem) {
        if (valor == null || valor.isBlank()) {
            throw new XmlNfseInvalidoException(mensagem);
        }
    }
}
