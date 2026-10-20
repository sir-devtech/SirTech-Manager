const PESOS_CPF = [11, 10, 9, 8, 7, 6, 5, 4, 3, 2];
const PESOS_CNPJ = [6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2];

export function somenteDigitos(valor: string): string {
  return valor.replace(/\D/g, '');
}

/** Mesma conta do backend: módulo 11, e rejeita número com todos os dígitos iguais. */
export function documentoValido(valor: string): boolean {
  const digitos = somenteDigitos(valor);
  if (digitos.length === 11) {
    return verificadores(digitos, PESOS_CPF);
  }
  if (digitos.length === 14) {
    return verificadores(digitos, PESOS_CNPJ);
  }
  return false;
}

function verificadores(digitos: string, pesos: number[]): boolean {
  if (new Set(digitos).size === 1) {
    return false;
  }
  const base = digitos.length - 2;
  const primeiro = calcular(digitos, base, pesos, 1);
  const segundo = calcular(digitos, base + 1, pesos, 0);
  return primeiro === Number(digitos[base]) && segundo === Number(digitos[base + 1]);
}

function calcular(digitos: string, quantidade: number, pesos: number[], deslocamento: number): number {
  let soma = 0;
  for (let i = 0; i < quantidade; i++) {
    soma += Number(digitos[i]) * pesos[i + deslocamento];
  }
  const resto = soma % 11;
  return resto < 2 ? 0 : 11 - resto;
}
