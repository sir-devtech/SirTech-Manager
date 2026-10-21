import { TestBed } from '@angular/core/testing';
import { MatDialog } from '@angular/material/dialog';
import { of } from 'rxjs';

import { CompetenciaService } from '../../core/competencia.service';
import { DespesaService } from '../../core/despesa.service';
import { Despesa } from '../../core/modelos';
import { Despesas } from './despesas';

const SERVIDOR: Despesa = {
  id: 1,
  descricao: 'Servidor DigitalOcean (demo)',
  categoria: 'SERVIDOR',
  valor: 150,
  competencia: '2026-10',
  recorrente: true,
};
const SIMPLES: Despesa = {
  id: 2,
  descricao: 'Simples Nacional (demo)',
  categoria: 'IMPOSTO',
  valor: 64.5,
  competencia: '2026-10',
  recorrente: false,
};

describe('Despesas', () => {
  const api = { listar: vi.fn(), excluir: vi.fn() };
  const dialog = { open: vi.fn() };

  function criar(lista: Despesa[] = [SERVIDOR, SIMPLES]) {
    api.listar.mockReturnValue(of(lista));
    TestBed.configureTestingModule({
      imports: [Despesas],
      providers: [
        { provide: DespesaService, useValue: api },
        { provide: CompetenciaService, useValue: { valor: () => '2026-10' } },
        { provide: MatDialog, useValue: dialog },
      ],
    });
    const fixture = TestBed.createComponent(Despesas);
    fixture.detectChanges();
    return { tela: fixture.componentInstance, html: fixture.nativeElement as HTMLElement };
  }

  function responderDialog(resposta: boolean | undefined): void {
    dialog.open.mockReturnValue({ afterClosed: () => of(resposta) });
  }

  beforeEach(() => {
    api.listar.mockReset();
    api.excluir.mockReset();
    dialog.open.mockReset();
  });

  it('pede as despesas do mês escolhido', () => {
    criar();

    expect(api.listar).toHaveBeenCalledWith('2026-10');
  });

  it('a despesa recorrente aparece como "Todo mês" e a avulsa como "Só este mês"', () => {
    const { html } = criar();

    const linhas = Array.from(html.querySelectorAll('tr.mat-mdc-row')).map((l) => l.textContent ?? '');
    expect(linhas[0]).toContain('Todo mês');
    expect(linhas[1]).toContain('Só este mês');
  });

  it('mostra o nome amigável da categoria, não o código do banco', () => {
    const { html } = criar();

    expect(html.textContent).toContain('Simples Nacional');
    expect(html.textContent).not.toContain('IMPOSTO');
  });

  it('a leitura rápida soma o mês: no plural e no singular', () => {
    const { tela } = criar();
    expect(tela.textoTotal()).toMatch(/^2 despesas, R\$\s214,50\.$/);

    tela.despesas.set([SERVIDOR]);
    expect(tela.textoTotal()).toMatch(/^1 despesa, R\$\s150,00\.$/);
  });

  it('mês sem despesa mostra a frase de vazio', () => {
    const { html } = criar([]);

    expect(html.querySelector('.vazio')?.textContent).toContain('Nenhuma despesa neste mês');
  });

  it('excluir pede confirmação antes de chamar a API', () => {
    api.excluir.mockReturnValue(of(undefined));
    const { tela } = criar();

    responderDialog(false);
    tela.excluir(SERVIDOR);
    expect(api.excluir).not.toHaveBeenCalled();

    responderDialog(true);
    tela.excluir(SERVIDOR);
    expect(api.excluir).toHaveBeenCalledWith(1);
  });

  it('a pergunta de exclusão cita a despesa pelo nome', () => {
    const { tela } = criar();
    responderDialog(false);

    tela.excluir(SERVIDOR);

    const dados = dialog.open.mock.calls[0][1].data;
    expect(dados.texto).toContain('Servidor DigitalOcean (demo)');
  });

  it('depois de excluir, a lista é recarregada', () => {
    api.excluir.mockReturnValue(of(undefined));
    const { tela } = criar();
    api.listar.mockClear();
    responderDialog(true);

    tela.excluir(SIMPLES);

    expect(api.listar).toHaveBeenCalledTimes(1);
  });

  it('a caixa de nova despesa já sabe em que mês vai lançar', () => {
    const { tela } = criar();
    responderDialog(undefined);

    tela.abrir(null);

    expect(dialog.open.mock.calls[0][1].data).toEqual({ despesa: null, competencia: '2026-10' });
  });

  it('só recarrega depois de salvar na caixa', () => {
    const { tela } = criar();
    api.listar.mockClear();

    responderDialog(undefined);
    tela.abrir(SERVIDOR);
    expect(api.listar).not.toHaveBeenCalled();

    responderDialog(true);
    tela.abrir(SERVIDOR);
    expect(api.listar).toHaveBeenCalledTimes(1);
  });
});
