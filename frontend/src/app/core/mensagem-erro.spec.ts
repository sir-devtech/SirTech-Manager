import { HttpErrorResponse } from '@angular/common/http';

import { mensagemDe } from './mensagem-erro';

describe('mensagemDe', () => {
  it('erros de campo do 400 viram uma frase só', () => {
    const erro = new HttpErrorResponse({
      status: 400,
      error: { detail: 'Um ou mais campos estão inválidos', erros: { documento: 'CPF ou CNPJ inválido' } },
    });

    expect(mensagemDe(erro)).toBe('documento: CPF ou CNPJ inválido');
  });

  it('sem erros de campo, usa o detail do ProblemDetail', () => {
    const erro = new HttpErrorResponse({ status: 409, error: { detail: 'Já existe um cliente com este CPF/CNPJ' } });

    expect(mensagemDe(erro)).toBe('Já existe um cliente com este CPF/CNPJ');
  });

  it('sem corpo (API fora do ar), mostra uma mensagem genérica', () => {
    expect(mensagemDe(new HttpErrorResponse({ status: 0 }))).toBe('Não foi possível completar esta ação.');
  });
});
