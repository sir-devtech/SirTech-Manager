import { TestBed } from '@angular/core/testing';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { of } from 'rxjs';

import { ClienteService } from '../../core/cliente.service';
import { CompetenciaService } from '../../core/competencia.service';
import { Nota, StatusNota } from '../../core/modelos';
import { NotaService } from '../../core/nota.service';
import { Notas } from './notas';

function nota(id: number, status: StatusNota, extra: Partial<Nota> = {}): Nota {
  return {
    id,
    clienteId: 1,
    clienteNome: `Cliente ${id}`,
    competencia: '2026-10',
    numero: status === 'PENDENTE' ? null : `NF-${id}`,
    valor: 300,
    dataEmissao: null,
    dataVencimento: '2026-10-10',
    dataPagamento: null,
    status,
    vencida: false,
    ...extra,
  };
}

describe('Notas', () => {
  const api = {
    listar: vi.fn(),
    cancelar: vi.fn(),
    importar: vi.fn(),
  };
  const clientes = { listar: vi.fn() };
  const avisos = { open: vi.fn() };
  const dialog = { open: vi.fn() };

  function responderDialog(resposta: boolean): void {
    dialog.open.mockReturnValue({ afterClosed: () => of(resposta) });
  }

  function criar() {
    TestBed.configureTestingModule({
      imports: [Notas],
      providers: [
        { provide: NotaService, useValue: api },
        { provide: ClienteService, useValue: clientes },
        { provide: CompetenciaService, useValue: { valor: () => '2026-10' } },
        { provide: MatDialog, useValue: dialog },
        { provide: MatSnackBar, useValue: avisos },
      ],
    });
    const fixture = TestBed.createComponent(Notas);
    fixture.detectChanges();
    return { fixture, tela: fixture.componentInstance, html: fixture.nativeElement as HTMLElement };
  }

  /** Texto dos botões de cada linha da tabela, na ordem em que aparecem. */
  function botoesPorLinha(html: HTMLElement): string[][] {
    return Array.from(html.querySelectorAll('tr.mat-mdc-row')).map((linha) =>
      Array.from(linha.querySelectorAll('button')).map((botao) => botao.textContent?.trim() ?? ''),
    );
  }

  beforeEach(() => {
    api.listar.mockReset();
    api.cancelar.mockReset();
    api.importar.mockReset();
    clientes.listar.mockReset();
    avisos.open.mockReset();
    dialog.open.mockReset();
    clientes.listar.mockReturnValue(of([]));
    api.listar.mockReturnValue(
      of([nota(1, 'PENDENTE'), nota(2, 'EMITIDA'), nota(3, 'PAGA'), nota(4, 'CANCELADA')]),
    );
  });

  it('os botões de cada linha seguem a situação da nota', () => {
    const { html } = criar();

    expect(botoesPorLinha(html)).toEqual([
      ['Emitir', 'Cancelar'], // pendente
      ['Pagar', 'Cancelar'], // emitida
      [], // paga não muda mais
      [], // cancelada encerrou
    ]);
  });

  it('pede as notas do mês escolhido, sem filtro no começo', () => {
    criar();

    expect(api.listar).toHaveBeenCalledWith('2026-10', undefined, undefined);
  });

  it('o filtro de situação refaz o pedido com o status certo', () => {
    const { fixture, tela } = criar();
    api.listar.mockClear();

    tela.escolherStatus('EMITIDA');
    fixture.detectChanges();

    expect(api.listar).toHaveBeenCalledWith('2026-10', 'EMITIDA', undefined);
  });

  it('o filtro de cliente refaz o pedido com o cliente certo', () => {
    const { fixture, tela } = criar();
    api.listar.mockClear();

    tela.escolherCliente(7);
    fixture.detectChanges();

    expect(api.listar).toHaveBeenCalledWith('2026-10', undefined, 7);
  });

  it('mostra a frase de vazio quando o mês não tem nota', () => {
    api.listar.mockReturnValue(of([]));

    const { html } = criar();

    expect(html.querySelector('.vazio')?.textContent).toContain('Nenhuma nota neste mês');
    expect(html.querySelector('table')).toBeNull();
  });

  it('a leitura rápida conta as pendentes, no singular e no plural', () => {
    const { tela } = criar();
    expect(tela.textoPendentes()).toBe('1 nota pendente neste mês.');

    tela.notas.set([nota(1, 'PENDENTE'), nota(2, 'PENDENTE'), nota(3, 'PAGA')]);
    expect(tela.textoPendentes()).toBe('2 notas pendentes neste mês.');
  });

  it('marca a nota vencida na coluna de vencimento', () => {
    api.listar.mockReturnValue(of([nota(1, 'EMITIDA', { vencida: true })]));

    const { html } = criar();

    expect(html.querySelector('.aviso')?.textContent).toContain('vencida');
  });

  it('cancelar só chama a API depois da confirmação', () => {
    api.cancelar.mockReturnValue(of(nota(1, 'CANCELADA')));
    const { tela } = criar();

    responderDialog(false);
    tela.cancelar(nota(1, 'PENDENTE'));
    expect(api.cancelar).not.toHaveBeenCalled();

    responderDialog(true);
    tela.cancelar(nota(1, 'PENDENTE'));
    expect(api.cancelar).toHaveBeenCalledWith(1);
  });

  it('depois de emitir com sucesso, a lista é recarregada', () => {
    const { tela } = criar();
    api.listar.mockClear();
    responderDialog(true);

    tela.emitir(nota(1, 'PENDENTE'));

    expect(api.listar).toHaveBeenCalledTimes(1);
  });

  it('se a caixa de emitir for fechada sem salvar, não recarrega', () => {
    const { tela } = criar();
    api.listar.mockClear();
    responderDialog(false);

    tela.emitir(nota(1, 'PENDENTE'));

    expect(api.listar).not.toHaveBeenCalled();
  });

  it('importar o XML avisa qual nota entrou e recarrega a lista', () => {
    api.importar.mockReturnValue(of(nota(9, 'EMITIDA', { numero: '10', clienteNome: 'Barbearia' })));
    const { tela } = criar();
    api.listar.mockClear();
    const arquivo = new File(['<xml/>'], 'nota.xml', { type: 'text/xml' });
    const entrada = { files: [arquivo], value: 'C:\\fakepath\\nota.xml' };

    tela.importar({ target: entrada } as unknown as Event);

    expect(api.importar).toHaveBeenCalledWith(arquivo);
    expect(avisos.open.mock.calls[0][0]).toBe('Nota 10 importada para Barbearia.');
    expect(api.listar).toHaveBeenCalledTimes(1);
    expect(entrada.value).toBe('');
  });

  it('sem arquivo escolhido, não importa nada', () => {
    const { tela } = criar();

    tela.importar({ target: { files: [] } } as unknown as Event);

    expect(api.importar).not.toHaveBeenCalled();
  });
});
