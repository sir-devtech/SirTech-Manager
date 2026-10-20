import { TestBed } from '@angular/core/testing';
import { of } from 'rxjs';

import { CompetenciaService } from '../../core/competencia.service';
import { FinanceiroService } from '../../core/financeiro.service';
import { Financeiro as Visao } from '../../core/modelos';
import { Financeiro, deslocar } from './financeiro';

const VAZIO = { aVencer: 0, valorAVencer: 0, ate30: 0, valorAte30: 0, de31a60: 0, valorDe31a60: 0, acimaDe60: 0, valorAcimaDe60: 0 };

function visao(parcial: Partial<Visao> = {}): Visao {
  return {
    inicio: '2026-05',
    fim: '2026-10',
    receitaPrevista: 4080,
    receitaRecebida: 3400,
    totalDespesas: 1705,
    lucroPrevisto: 2375,
    lucroRealizado: 1695,
    meses: [],
    porCliente: [{ chave: '1', nome: 'Barbearia', valor: 1800, parte: 0.44 }],
    porProduto: [],
    porCategoria: [],
    atraso: VAZIO,
    ...parcial,
  };
}

describe('Financeiro', () => {
  const api = { consultar: vi.fn() };

  function criar() {
    TestBed.configureTestingModule({
      imports: [Financeiro],
      providers: [
        { provide: FinanceiroService, useValue: api },
        { provide: CompetenciaService, useValue: { valor: () => '2026-10' } },
      ],
    });
    return TestBed.createComponent(Financeiro).componentInstance;
  }

  beforeEach(() => {
    api.consultar.mockReset();
    api.consultar.mockReturnValue(of(visao()));
  });

  it('abre nos últimos 6 meses, terminando no mês escolhido', () => {
    criar();

    expect(api.consultar).toHaveBeenCalledWith('2026-05', '2026-10');
  });

  it('"Este ano" vai de janeiro a dezembro', () => {
    const tela = criar();

    tela.escolher('ano');

    expect(api.consultar).toHaveBeenLastCalledWith('2026-01', '2026-12');
  });

  it('intervalo com início depois do fim não busca nada', () => {
    const tela = criar();
    api.consultar.mockClear();

    tela.inicio.set('2026-09');
    tela.fim.set('2026-03');

    expect(tela.aplicarIntervalo()).toBe(false);
    expect(api.consultar).not.toHaveBeenCalled();
  });

  it('o demonstrativo fecha: previsto − a receber = recebido; recebido − despesas = resultado', () => {
    const tela = criar();
    const [previsto, aReceber, recebido, despesas, resultado] = tela.demonstrativo().map((l) => l.valor);

    expect(previsto + aReceber).toBe(recebido);
    expect(recebido + despesas).toBe(resultado);
    expect(tela.margem()).toBeCloseTo(1695 / 3400);
  });

  it('sem nada recebido não inventa margem', () => {
    api.consultar.mockReturnValue(of(visao({ receitaRecebida: 0, lucroRealizado: -220 })));
    const tela = criar();

    expect(tela.margem()).toBeNull();
  });

  it('avisa quando um cliente passa de metade da receita', () => {
    api.consultar.mockReturnValue(
      of(visao({ porCliente: [{ chave: '1', nome: 'Barbearia', valor: 600, parte: 0.6 }] })),
    );
    const tela = criar();

    expect(tela.textoLeitura()).toBe('Barbearia passa de metade da receita.');
  });

  it('deslocar atravessa a virada do ano', () => {
    expect(deslocar('2026-02', -5)).toBe('2025-09');
    expect(deslocar('2026-12', 1)).toBe('2027-01');
  });
});
