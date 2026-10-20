package br.com.sirtech.manager.shared.validacao;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class CpfOuCnpjValidatorTest {

    private final CpfOuCnpjValidator validator = new CpfOuCnpjValidator();

    @ParameterizedTest
    @ValueSource(strings = {
            "52998224725",          // CPF sem máscara
            "529.982.247-25",       // CPF com máscara
            "11222333000181",       // CNPJ sem máscara
            "11.444.777/0001-61"    // CNPJ com máscara
    })
    void aceitaDocumentosValidos(String documento) {
        assertThat(validator.isValid(documento, null)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "52998224724",          // CPF com dígito verificador errado
            "11222333000182",       // CNPJ com dígito verificador errado
            "11111111111",          // todos os dígitos iguais
            "00000000000000",
            "123",                  // tamanho inválido
            "",
            "abc.def.ghi-jk"
    })
    void rejeitaDocumentosInvalidos(String documento) {
        assertThat(validator.isValid(documento, null)).isFalse();
    }

    @ParameterizedTest
    @NullSource
    void nuloEhValidoPoisObrigatoriedadeEhDoNotBlank(String documento) {
        assertThat(validator.isValid(documento, null)).isTrue();
    }
}
