import { AbstractControl, ValidationErrors } from '@angular/forms';
import { documentoValido } from './documento';

export function cpfOuCnpj(controle: AbstractControl): ValidationErrors | null {
  const valor = String(controle.value ?? '').trim();
  if (!valor) {
    return null;
  }
  return documentoValido(valor) ? null : { documento: true };
}
