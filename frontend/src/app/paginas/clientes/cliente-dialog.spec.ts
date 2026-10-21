import { TestBed } from '@angular/core/testing';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { of, throwError } from 'rxjs';

import { ClienteService } from '../../core/cliente.service';
import { Cliente } from '../../core/modelos';
import { ClienteDialog } from './cliente-dialog';

const EXISTENTE: Cliente = {
  id: 4,
  nome: 'Pet Shop Amigo Fiel (fictícia)',
  documento: '11444777000161',
  email: 'contato@amigofiel.example',
  produto: 'SIR_PET',
  valorMensal: 200,
  diaVencimento: 10,
  ativo: true,
  criadoEm: '2026-10-01T09:00:00',
};

describe('ClienteDialog', () => {
  const api = { criar: vi.fn(), atualizar: vi.fn() };
  const ref = { close: vi.fn() };

  function criar(cliente: Cliente | null = null) {
    TestBed.configureTestingModule({
      imports: [ClienteDialog],
      providers: [
        { provide: ClienteService, useValue: api },
        { provide: MatDialogRef, useValue: ref },
        { provide: MAT_DIALOG_DATA, useValue: cliente },
      ],
    });
    const fixture = TestBed.createComponent(ClienteDialog);
    fixture.detectChanges();
    return { tela: fixture.componentInstance, html: fixture.nativeElement as HTMLElement };
  }

  function preencher(tela: ClienteDialog, extra: Record<string, unknown> = {}): void {
    tela.form.patchValue({
      nome: 'Barbearia Navalha de Ouro (fictícia)',
      documento: '11.222.333/0001-81',
      valorMensal: 300,
      diaVencimento: 10,
      ...extra,
    });
  }

  beforeEach(() => {
    api.criar.mockReset();
    api.atualizar.mockReset();
    ref.close.mockReset();
  });

  it('novo cliente começa vazio, com valores iniciais seguros', () => {
    const { tela, html } = criar();

    expect(html.querySelector('h2')?.textContent).toContain('Novo cliente');
    expect(tela.form.controls.nome.value).toBe('');
    expect(tela.form.controls.diaVencimento.value).toBe(10);
    expect(tela.form.invalid).toBe(true);
  });

  it('editar abre a caixa com os dados do cliente', () => {
    const { tela, html } = criar(EXISTENTE);

    expect(html.querySelector('h2')?.textContent).toContain('Editar cliente');
    expect(tela.form.controls.nome.value).toBe('Pet Shop Amigo Fiel (fictícia)');
    expect(tela.form.valid).toBe(true);
  });

  it('CPF ou CNPJ com dígito errado deixa o formulário inválido e não salva', () => {
    const { tela } = criar();
    preencher(tela, { documento: '11.222.333/0001-82' });

    tela.salvar();

    expect(tela.form.controls.documento.hasError('documento')).toBe(true);
    expect(api.criar).not.toHaveBeenCalled();
  });

  it('CPF válido é aceito, com ou sem máscara', () => {
    const { tela } = criar();

    preencher(tela, { documento: '529.982.247-25' });
    expect(tela.form.controls.documento.valid).toBe(true);

    preencher(tela, { documento: '52998224725' });
    expect(tela.form.controls.documento.valid).toBe(true);
  });

  it.each([0, 29, 31])('dia de vencimento %i é recusado (só vale de 1 a 28)', (dia) => {
    const { tela } = criar();
    preencher(tela, { diaVencimento: dia });

    expect(tela.form.controls.diaVencimento.invalid).toBe(true);
  });

  it.each([1, 28])('dia de vencimento %i é aceito (limites da faixa)', (dia) => {
    const { tela } = criar();
    preencher(tela, { diaVencimento: dia });

    expect(tela.form.controls.diaVencimento.valid).toBe(true);
  });

  it('valor mensal zero é recusado', () => {
    const { tela } = criar();
    preencher(tela, { valorMensal: 0 });

    expect(tela.form.controls.valorMensal.invalid).toBe(true);
  });

  it('e-mail mal escrito é recusado, mas e-mail vazio é permitido', () => {
    const { tela } = criar();

    preencher(tela, { email: 'sem-arroba' });
    expect(tela.form.controls.email.invalid).toBe(true);

    preencher(tela, { email: '' });
    expect(tela.form.controls.email.valid).toBe(true);
  });

  it('e-mail só com espaços é recusado: o formulário avisa em vez de gravar lixo', () => {
    const { tela } = criar();
    preencher(tela, { email: '   ' });

    tela.salvar();

    expect(tela.form.controls.email.invalid).toBe(true);
    expect(api.criar).not.toHaveBeenCalled();
  });

  it('cadastrar manda os dados limpos: nome sem espaços, e-mail vazio como null, números de verdade', () => {
    api.criar.mockReturnValue(of(EXISTENTE));
    const { tela } = criar();
    preencher(tela, { nome: '  Barbearia X  ', email: '', valorMensal: '300.5', diaVencimento: '15' });

    tela.salvar();

    expect(api.criar).toHaveBeenCalledWith({
      nome: 'Barbearia X',
      documento: '11.222.333/0001-81',
      email: null,
      produto: 'SIR_BARBER',
      valorMensal: 300.5,
      diaVencimento: 15,
    });
    expect(ref.close).toHaveBeenCalledWith(true);
  });

  it('editar chama atualizar com o id do cliente, e não criar', () => {
    api.atualizar.mockReturnValue(of(EXISTENTE));
    const { tela } = criar(EXISTENTE);

    tela.salvar();

    expect(api.atualizar).toHaveBeenCalledWith(4, expect.objectContaining({ documento: '11444777000161' }));
    expect(api.criar).not.toHaveBeenCalled();
  });

  it('se a API recusar (ex.: CNPJ repetido), a caixa fica aberta para corrigir', () => {
    api.criar.mockReturnValue(throwError(() => new Error('409')));
    const { tela } = criar();
    preencher(tela);

    tela.salvar();

    expect(ref.close).not.toHaveBeenCalled();
    expect(tela.salvando).toBe(false);
  });
});
