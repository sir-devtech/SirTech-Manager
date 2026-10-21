import { TestBed } from '@angular/core/testing';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { of, throwError } from 'rxjs';

import { DespesaService } from '../../core/despesa.service';
import { Despesa } from '../../core/modelos';
import { DespesaDialog, DespesaDialogDados } from './despesa-dialog';

const EXISTENTE: Despesa = {
  id: 3,
  descricao: 'Domínio (demo)',
  categoria: 'DOMINIO',
  valor: 40,
  competencia: '2026-10',
  recorrente: true,
};

describe('DespesaDialog', () => {
  const api = { criar: vi.fn(), atualizar: vi.fn() };
  const ref = { close: vi.fn() };

  function criar(despesa: Despesa | null = null) {
    const dados: DespesaDialogDados = { despesa, competencia: '2026-10' };
    TestBed.configureTestingModule({
      imports: [DespesaDialog],
      providers: [
        { provide: DespesaService, useValue: api },
        { provide: MatDialogRef, useValue: ref },
        { provide: MAT_DIALOG_DATA, useValue: dados },
      ],
    });
    const fixture = TestBed.createComponent(DespesaDialog);
    fixture.detectChanges();
    return fixture.componentInstance;
  }

  beforeEach(() => {
    api.criar.mockReset();
    api.atualizar.mockReset();
    ref.close.mockReset();
  });

  it('nova despesa começa como recorrente de servidor, o caso mais comum', () => {
    const tela = criar();

    expect(tela.form.controls.categoria.value).toBe('SERVIDOR');
    expect(tela.form.controls.recorrente.value).toBe(true);
    expect(tela.form.controls.valor.value).toBe(150);
  });

  it('sem descrição não salva', () => {
    const tela = criar();

    tela.salvar();

    expect(tela.form.controls.descricao.hasError('required')).toBe(true);
    expect(api.criar).not.toHaveBeenCalled();
  });

  it('valor zero ou negativo é recusado', () => {
    const tela = criar();

    tela.form.patchValue({ descricao: 'Teste', valor: 0 });
    expect(tela.form.controls.valor.invalid).toBe(true);

    tela.form.patchValue({ valor: -10 });
    expect(tela.form.controls.valor.invalid).toBe(true);
  });

  it('cadastrar manda o mês que veio da tela, não um mês escolhido na caixa', () => {
    api.criar.mockReturnValue(of(EXISTENTE));
    const tela = criar();
    tela.form.patchValue({
      descricao: '  Simples Nacional  ',
      categoria: 'IMPOSTO',
      valor: 64.5,
      recorrente: false,
    });

    tela.salvar();

    expect(api.criar).toHaveBeenCalledWith({
      descricao: 'Simples Nacional',
      categoria: 'IMPOSTO',
      valor: 64.5,
      competencia: '2026-10',
      recorrente: false,
    });
    expect(ref.close).toHaveBeenCalledWith(true);
  });

  it('editar chama atualizar com o id e não criar uma nova', () => {
    api.atualizar.mockReturnValue(of(EXISTENTE));
    const tela = criar(EXISTENTE);

    tela.salvar();

    expect(api.atualizar).toHaveBeenCalledWith(3, expect.objectContaining({ descricao: 'Domínio (demo)' }));
    expect(api.criar).not.toHaveBeenCalled();
  });

  it('se a API recusar (ex.: recorrente repetida), a caixa fica aberta', () => {
    api.criar.mockReturnValue(throwError(() => new Error('409')));
    const tela = criar();
    tela.form.patchValue({ descricao: 'Servidor' });

    tela.salvar();

    expect(ref.close).not.toHaveBeenCalled();
    expect(tela.salvando).toBe(false);
  });
});
