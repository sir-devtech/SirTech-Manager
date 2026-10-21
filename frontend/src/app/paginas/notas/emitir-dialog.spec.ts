import { TestBed } from '@angular/core/testing';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { of, throwError } from 'rxjs';

import { Nota } from '../../core/modelos';
import { NotaService } from '../../core/nota.service';
import { EmitirDialog } from './emitir-dialog';

const NOTA: Nota = {
  id: 5,
  clienteId: 1,
  clienteNome: 'Barbearia Navalha de Ouro (fictícia)',
  competencia: '2026-10',
  numero: null,
  valor: 300,
  dataEmissao: null,
  dataVencimento: '2026-10-10',
  dataPagamento: null,
  status: 'PENDENTE',
  vencida: false,
};

describe('EmitirDialog', () => {
  const api = { emitir: vi.fn() };
  const ref = { close: vi.fn() };

  function criar() {
    TestBed.configureTestingModule({
      imports: [EmitirDialog],
      providers: [
        { provide: NotaService, useValue: api },
        { provide: MatDialogRef, useValue: ref },
        { provide: MAT_DIALOG_DATA, useValue: NOTA },
      ],
    });
    const fixture = TestBed.createComponent(EmitirDialog);
    fixture.detectChanges();
    return fixture.componentInstance;
  }

  beforeEach(() => {
    api.emitir.mockReset();
    ref.close.mockReset();
  });

  it('sem número, o formulário é inválido e a API não é chamada', () => {
    const tela = criar();

    tela.salvar();

    expect(tela.form.controls.numero.hasError('required')).toBe(true);
    expect(api.emitir).not.toHaveBeenCalled();
  });

  it('sem data de emissão, o formulário é inválido', () => {
    const tela = criar();
    tela.form.patchValue({ numero: '10' });
    tela.form.controls.dataEmissao.setValue(null as unknown as Date);

    tela.salvar();

    expect(tela.form.invalid).toBe(true);
    expect(api.emitir).not.toHaveBeenCalled();
  });

  it('manda número sem espaços e a data em AAAA-MM-DD, e fecha a caixa', () => {
    api.emitir.mockReturnValue(of(NOTA));
    const tela = criar();
    tela.form.setValue({ numero: '  10  ', dataEmissao: new Date(2026, 9, 10) });

    tela.salvar();

    expect(api.emitir).toHaveBeenCalledWith(5, '10', '2026-10-10');
    expect(ref.close).toHaveBeenCalledWith(true);
  });

  it('se a API recusar, a caixa continua aberta e o botão volta a funcionar', () => {
    api.emitir.mockReturnValue(throwError(() => new Error('422')));
    const tela = criar();
    tela.form.setValue({ numero: '10', dataEmissao: new Date(2026, 9, 10) });

    tela.salvar();

    expect(ref.close).not.toHaveBeenCalled();
    expect(tela.salvando).toBe(false);
  });
});
