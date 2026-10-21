import { TestBed } from '@angular/core/testing';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { of, throwError } from 'rxjs';

import { Nota } from '../../core/modelos';
import { NotaService } from '../../core/nota.service';
import { PagarDialog } from './pagar-dialog';

const NOTA: Nota = {
  id: 8,
  clienteId: 1,
  clienteNome: 'Pet Shop Amigo Fiel (fictícia)',
  competencia: '2026-10',
  numero: '11',
  valor: 200,
  dataEmissao: '2026-10-05',
  dataVencimento: '2026-10-10',
  dataPagamento: null,
  status: 'EMITIDA',
  vencida: false,
};

describe('PagarDialog', () => {
  const api = { pagar: vi.fn() };
  const ref = { close: vi.fn() };

  function criar() {
    TestBed.configureTestingModule({
      imports: [PagarDialog],
      providers: [
        { provide: NotaService, useValue: api },
        { provide: MatDialogRef, useValue: ref },
        { provide: MAT_DIALOG_DATA, useValue: NOTA },
      ],
    });
    const fixture = TestBed.createComponent(PagarDialog);
    fixture.detectChanges();
    return { tela: fixture.componentInstance, html: fixture.nativeElement as HTMLElement };
  }

  beforeEach(() => {
    api.pagar.mockReset();
    ref.close.mockReset();
  });

  it('mostra de quem é a nota que está sendo paga', () => {
    const { html } = criar();

    expect(html.textContent).toContain('Pet Shop Amigo Fiel (fictícia)');
    expect(html.textContent).toContain('nota 11');
  });

  it('a data do pagamento já vem preenchida com hoje', () => {
    const { tela } = criar();

    const data = tela.form.controls.dataPagamento.value;
    expect(data.toDateString()).toBe(new Date().toDateString());
  });

  it('manda a data em AAAA-MM-DD e fecha a caixa', () => {
    api.pagar.mockReturnValue(of(NOTA));
    const { tela } = criar();
    tela.form.setValue({ dataPagamento: new Date(2026, 9, 12) });

    tela.salvar();

    expect(api.pagar).toHaveBeenCalledWith(8, '2026-10-12');
    expect(ref.close).toHaveBeenCalledWith(true);
  });

  it('sem data, não chama a API', () => {
    const { tela } = criar();
    tela.form.controls.dataPagamento.setValue(null as unknown as Date);

    tela.salvar();

    expect(api.pagar).not.toHaveBeenCalled();
  });

  it('se o servidor recusar (ex.: pagamento antes da emissão), a caixa fica aberta', () => {
    api.pagar.mockReturnValue(throwError(() => new Error('422')));
    const { tela } = criar();

    tela.salvar();

    expect(ref.close).not.toHaveBeenCalled();
    expect(tela.salvando).toBe(false);
  });
});
