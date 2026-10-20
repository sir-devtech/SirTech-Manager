package br.com.sirtech.manager.shared.validacao;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CpfOuCnpjValidator implements ConstraintValidator<CpfOuCnpj, String> {

    private static final int[] PESOS_CPF = {11, 10, 9, 8, 7, 6, 5, 4, 3, 2};
    private static final int[] PESOS_CNPJ = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};

    @Override
    public boolean isValid(String valor, ConstraintValidatorContext context) {
        if (valor == null) {
            return true;
        }
        String digitos = somenteDigitos(valor);
        return switch (digitos.length()) {
            case 11 -> digitosVerificadoresValidos(digitos, PESOS_CPF);
            case 14 -> digitosVerificadoresValidos(digitos, PESOS_CNPJ);
            default -> false;
        };
    }

    public static String somenteDigitos(String valor) {
        return valor.replaceAll("\\D", "");
    }

    /**
     * CPF e CNPJ usam o mesmo algoritmo (módulo 11), mudando só os pesos.
     * O 1º dígito verificador usa os pesos a partir da 2ª posição; o 2º usa todos.
     */
    private static boolean digitosVerificadoresValidos(String digitos, int[] pesos) {
        if (digitos.chars().distinct().count() == 1) {
            return false; // 000.000.000-00, 111..., etc. passam na conta mas são inválidos
        }
        int tamanhoBase = digitos.length() - 2;
        int primeiro = calcularDigito(digitos, tamanhoBase, pesos, 1);
        int segundo = calcularDigito(digitos, tamanhoBase + 1, pesos, 0);
        return primeiro == Character.getNumericValue(digitos.charAt(tamanhoBase))
                && segundo == Character.getNumericValue(digitos.charAt(tamanhoBase + 1));
    }

    private static int calcularDigito(String digitos, int quantidade, int[] pesos, int deslocamentoPeso) {
        int soma = 0;
        for (int i = 0; i < quantidade; i++) {
            soma += Character.getNumericValue(digitos.charAt(i)) * pesos[i + deslocamentoPeso];
        }
        int resto = soma % 11;
        return resto < 2 ? 0 : 11 - resto;
    }
}
