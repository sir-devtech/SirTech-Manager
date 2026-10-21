import { TestBed } from '@angular/core/testing';
import { MatSnackBar } from '@angular/material/snack-bar';
import { of } from 'rxjs';

import { CompetenciaService } from '../../core/competencia.service';
import { Nota, Resumo } from '../../core/modelos';
import { NotaService } from '../../core/nota.service';
import { ResumoService } from '../../core/resumo.service';
import { Painel } from './painel';

function resumo(extra: Partial<Resumo> = {}): Resumo {
  return {
    competencia: '2026-10',
    receitaPrevista: 680,
    receitaRecebida: 0,
    totalDespesas: 220,
    lucroPrevisto: 460,
    lucroRealizado: -220,
    notasPendentesEmissao: 3,
    notasVencidas: 0,
    ...extra,
  };
}

function nota(id: number, extra: Partial<Nota> = {}): Nota {
  return {
    id,
    clienteId: id,
    clienteNome: `Cliente ${id}`,
    competencia: '2026-10',
    numero: null,
    valor: 200,
    dataEmissao: null,
    dataVencimento: '2026-10-10',
    dataPagamento: null,
    status: 'PENDENTE',
    vencida: false,
    ...extra,
  };
}

describe('Painel', () => {
  const resumoApi = { buscar: vi.fn(), backup: vi.fn() };
  const notasApi = { listar: vi.fn(), gerar: vi.fn() };
  const avisos = { open: vi.fn() };

  function criar(dadosResumo: Resumo = resumo(), notas: Nota[] = [nota(1), nota(2), nota(3)]) {
    resumoApi.buscar.mockReturnValue(of(dadosResumo));
    notasApi.listar.mockReturnValue(of(notas));
    TestBed.configureTestingModule({
      imports: [Painel],
      providers: [
        { provide: ResumoService, useValue: resumoApi },
        { provide: NotaService, useValue: notasApi },
        { provide: CompetenciaService, useValue: { valor: () => '2026-10' } },
        { provide: MatSnackBar, useValue: avisos },
      ],
    });
    const fixture = TestBed.createComponent(Painel);
    fixture.detectChanges();
    return { fixture, tela: fixture.componentInstance, html: fixture.nativeElement as HTMLElement };
  }

  function card(html: HTMLElement, nome: string): string {
    return html.querySelector(`article.card.${nome}`)?.textContent?.replace(/\s+/g, ' ').trim() ?? '';
  }

  beforeEach(() => {
    resumoApi.buscar.mockReset();
    resumoApi.backup.mockReset();
    notasApi.listar.mockReset();
    notasApi.gerar.mockReset();
    avisos.open.mockReset();
  });

  afterEach(() => vi.useRealTimers());

  describe('cards do mês', () => {
    it('o card de despesas mostra quanto elas pesam no previsto', () => {
      const { html } = criar(resumo({ receitaPrevista: 680, totalDespesas: 220 }));

      expect(card(html, 'despesas')).toContain('32% do previsto');
    });

    it('sem previsto não divide por zero: mostra o texto simples', () => {
      const { html } = criar(resumo({ receitaPrevista: 0, totalDespesas: 150 }));

      const texto = card(html, 'despesas');
      expect(texto).toContain('Tudo o que saiu no mês');
      expect(texto).not.toContain('Infinity');
      expect(texto).not.toContain('NaN');
    });

    it('lucro negativo pinta o card de vermelho; positivo, não', () => {
      const negativo = criar(resumo({ lucroRealizado: -220 }));
      expect(negativo.html.querySelector('article.lucro')?.classList.contains('negativo')).toBe(true);
      TestBed.resetTestingModule();

      const positivo = criar(resumo({ lucroRealizado: 400 }));
      expect(positivo.html.querySelector('article.lucro')?.classList.contains('negativo')).toBe(false);
    });

    it('o card de previsto diz quantas notas faltam emitir', () => {
      const { html } = criar(resumo({ notasPendentesEmissao: 3 }));

      expect(card(html, 'previsto')).toContain('3 nota(s) ainda por emitir');
    });

    it('a barra de recebido é a fração do previsto e nunca passa de 100%', () => {
      const { tela } = criar(resumo({ receitaPrevista: 680, receitaRecebida: 340 }));
      expect(tela.fracaoRecebida()).toBe(0.5);

      tela.resumo.set(resumo({ receitaPrevista: 100, receitaRecebida: 250 }));
      expect(tela.fracaoRecebida()).toBe(1);

      tela.resumo.set(resumo({ receitaPrevista: 0, receitaRecebida: 0 }));
      expect(tela.fracaoRecebida()).toBe(0);
    });
  });

  describe('aviso de atraso', () => {
    it('só aparece quando existe nota vencida, com a soma dos valores', () => {
      const notas = [nota(1, { vencida: true, valor: 300 }), nota(2, { vencida: true, valor: 200 }), nota(3)];
      const { html } = criar(resumo({ notasVencidas: 2 }), notas);

      const alerta = html.querySelector('.alerta')?.textContent?.replace(/\s+/g, ' ') ?? '';
      expect(alerta).toContain('2 nota(s) vencida(s)');
      expect(alerta).toMatch(/R\$\s?500,00|\$500\.00/);
    });

    it('sem nota vencida, o aviso não existe', () => {
      const { html } = criar(resumo(), [nota(1), nota(2)]);

      expect(html.querySelector('.alerta')).toBeNull();
      expect(html.textContent).toContain('Nenhuma vencida.');
    });

    it('calcula os dias de atraso a partir de hoje', () => {
      vi.useFakeTimers({ toFake: ['Date'] });
      vi.setSystemTime(new Date(2026, 9, 20, 15, 30));
      const { tela } = criar();

      expect(tela.diasDeAtraso(nota(1, { dataVencimento: '2026-10-10' }))).toBe(10);
      expect(tela.diasDeAtraso(nota(1, { dataVencimento: '2026-10-19' }))).toBe(1);
    });
  });

  describe('listas e gráfico', () => {
    it('lista as notas pendentes e só elas', () => {
      const notas = [nota(1), nota(2, { status: 'EMITIDA' }), nota(3, { status: 'PAGA' })];
      const { tela } = criar(resumo(), notas);

      expect(tela.pendentes().map((n) => n.id)).toEqual([1]);
    });

    it('a leitura rápida conta as pendentes no singular e no plural', () => {
      const { tela } = criar(resumo({ notasPendentesEmissao: 1 }));
      expect(tela.textoPendentes()).toBe('1 nota ainda por emitir.');

      tela.resumo.set(resumo({ notasPendentesEmissao: 3 }));
      expect(tela.textoPendentes()).toBe('3 notas ainda por emitir.');
    });

    it('o gráfico mostra os últimos 6 meses, terminando no mês escolhido', () => {
      const { tela } = criar();

      const competencias = resumoApi.buscar.mock.calls.map((chamada) => chamada[0]);
      expect(competencias).toEqual(['2026-05', '2026-06', '2026-07', '2026-08', '2026-09', '2026-10']);
      expect(tela.historico()).toHaveLength(6);
      expect(tela.historico().filter((mes) => mes.atual)).toHaveLength(1);
      expect(tela.historico()[5].atual).toBe(true);
    });

    it('a altura das barras é proporcional ao maior valor do período', () => {
      const { tela } = criar(resumo({ receitaPrevista: 800, receitaRecebida: 400, totalDespesas: 200 }));

      expect(tela.altura(800)).toBe('100%');
      expect(tela.altura(400)).toBe('50%');
      expect(tela.altura(200)).toBe('25%');
    });
  });

  describe('ações', () => {
    it('gerar notas avisa o que foi criado e recarrega o painel', () => {
      notasApi.gerar.mockReturnValue(of({ notasCriadas: 1, despesasReplicadas: 2 }));
      const { tela } = criar();
      notasApi.listar.mockClear();

      tela.gerar();

      expect(notasApi.gerar).toHaveBeenCalledWith('2026-10');
      expect(avisos.open.mock.calls[0][0]).toBe('Notas novas: 1. Despesas copiadas: 2.');
      expect(notasApi.listar).toHaveBeenCalledTimes(1);
    });

    it('gerar de novo no mesmo mês não cria nada e o aviso diz zero', () => {
      notasApi.gerar.mockReturnValue(of({ notasCriadas: 0, despesasReplicadas: 0 }));
      const { tela } = criar();

      tela.gerar();

      expect(avisos.open.mock.calls[0][0]).toBe('Notas novas: 0. Despesas copiadas: 0.');
    });

    it('fazer backup avisa o nome do arquivo gravado', () => {
      resumoApi.backup.mockReturnValue(
        of({ arquivo: 'sirtech-20261020-153000.sql', tamanhoBytes: 10240, criadoEm: '2026-10-20T15:30:00' }),
      );
      const { tela } = criar();

      tela.backup();

      expect(avisos.open.mock.calls[0][0]).toBe('Backup gravado: sirtech-20261020-153000.sql');
    });
  });
});
