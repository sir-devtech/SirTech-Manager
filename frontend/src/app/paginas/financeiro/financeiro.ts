import { CurrencyPipe, PercentPipe } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';

import { LeituraRapida } from '../../compartilhado/leitura-rapida';
import { CompetenciaService } from '../../core/competencia.service';
import { FinanceiroService } from '../../core/financeiro.service';
import {
  CATEGORIAS,
  CategoriaDespesa,
  Fatia,
  MesFinanceiro,
  PRODUTOS,
  Produto,
  Financeiro as Visao,
  rotuloDe,
} from '../../core/modelos';

export type Atalho = 'mes' | 'seis' | 'ano' | 'intervalo';

/** Uma linha do demonstrativo: rótulo, valor e se é um total (linha mais forte). */
interface LinhaDemonstrativo {
  rotulo: string;
  valor: number;
  total?: boolean;
  sinal?: '+' | '−' | '=';
}

/**
 * Leitura de um período, no formato de um livro-caixa. Não repete o Painel:
 * o Painel opera o mês; aqui se analisa o período (por padrão, os últimos 6 meses).
 */
@Component({
  selector: 'app-financeiro',
  imports: [CurrencyPipe, PercentPipe, MatButtonModule, LeituraRapida],
  templateUrl: './financeiro.html',
  styleUrl: './financeiro.scss',
})
export class Financeiro {
  private readonly api = inject(FinanceiroService);
  private readonly competencia = inject(CompetenciaService);

  readonly atalhos: { valor: Atalho; rotulo: string }[] = [
    { valor: 'mes', rotulo: 'Este mês' },
    { valor: 'seis', rotulo: 'Últimos 6 meses' },
    { valor: 'ano', rotulo: 'Este ano' },
    { valor: 'intervalo', rotulo: 'Personalizado' },
  ];

  readonly atalho = signal<Atalho>('seis');
  readonly inicio = signal(deslocar(this.competencia.valor(), -5));
  readonly fim = signal(this.competencia.valor());
  readonly dados = signal<Visao | null>(null);
  readonly carregou = signal(false);

  readonly aReceber = computed(() => {
    const dados = this.dados();
    return dados ? Math.max(0, dados.receitaPrevista - dados.receitaRecebida) : 0;
  });

  /** Quanto do que entrou sobrou depois das despesas. Sem receita, não há margem. */
  readonly margem = computed(() => {
    const dados = this.dados();
    return dados && dados.receitaRecebida > 0 ? dados.lucroRealizado / dados.receitaRecebida : null;
  });

  readonly demonstrativo = computed<LinhaDemonstrativo[]>(() => {
    const dados = this.dados();
    if (!dados) {
      return [];
    }
    return [
      { rotulo: 'Receita prevista', valor: dados.receitaPrevista, sinal: '+' },
      { rotulo: 'Ainda a receber', valor: -this.aReceber(), sinal: '−' },
      { rotulo: 'Receita recebida', valor: dados.receitaRecebida, total: true, sinal: '=' },
      { rotulo: 'Despesas', valor: -dados.totalDespesas, sinal: '−' },
      { rotulo: 'Resultado realizado', valor: dados.lucroRealizado, total: true, sinal: '=' },
    ];
  });

  readonly maiorCliente = computed<Fatia | null>(() => this.dados()?.porCliente[0] ?? null);

  readonly maiorFatiaCliente = computed(() => Math.max(0.0001, ...(this.dados()?.porCliente ?? []).map((f) => f.parte)));
  readonly maiorFatiaCategoria = computed(() =>
    Math.max(0.0001, ...(this.dados()?.porCategoria ?? []).map((f) => f.parte)),
  );

  readonly temAtraso = computed(() => {
    const atraso = this.dados()?.atraso;
    return !!atraso && atraso.aVencer + atraso.ate30 + atraso.de31a60 + atraso.acimaDe60 > 0;
  });

  constructor() {
    this.buscar();
  }

  escolher(atalho: Atalho): void {
    const mes = this.competencia.valor();
    this.atalho.set(atalho);
    if (atalho === 'mes') {
      this.definir(mes, mes);
    } else if (atalho === 'seis') {
      this.definir(deslocar(mes, -5), mes);
    } else if (atalho === 'ano') {
      const ano = mes.slice(0, 4);
      this.definir(`${ano}-01`, `${ano}-12`);
    }
  }

  /** O intervalo só é buscado quando início não passa do fim. */
  aplicarIntervalo(): boolean {
    if (!this.inicio() || !this.fim() || this.inicio() > this.fim()) {
      return false;
    }
    this.buscar();
    return true;
  }

  textoLeitura(): string {
    const maior = this.maiorCliente();
    if (!maior) {
      return 'Sem receita neste período.';
    }
    return maior.parte > 0.5
      ? `${maior.nome} passa de metade da receita.`
      : 'Nenhum cliente passa de metade da receita.';
  }

  resultadoDoMes(mes: MesFinanceiro): number {
    return mes.receitaRecebida - mes.totalDespesas;
  }

  /** Largura da barra relativa à maior fatia do grupo, para a maior encostar no fim. */
  largura(parte: number, maior: number): string {
    return `${Math.max(2, (parte / maior) * 100)}%`;
  }

  rotuloMes(competencia: string): string {
    const [ano, mes] = competencia.split('-').map(Number);
    const nome = new Intl.DateTimeFormat('pt-BR', { month: 'short' })
      .format(new Date(ano, mes - 1, 1))
      .replace('.', '');
    return `${nome.charAt(0).toUpperCase()}${nome.slice(1)}/${String(ano).slice(2)}`;
  }

  nomeProduto(fatia: Fatia): string {
    return rotuloDe(PRODUTOS, fatia.chave as Produto);
  }

  nomeCategoria(fatia: Fatia): string {
    return rotuloDe(CATEGORIAS, fatia.chave as CategoriaDespesa);
  }

  private definir(inicio: string, fim: string): void {
    this.inicio.set(inicio);
    this.fim.set(fim);
    this.buscar();
  }

  private buscar(): void {
    this.carregou.set(false);
    this.api.consultar(this.inicio(), this.fim()).subscribe({
      next: (valor) => {
        this.dados.set(valor);
        this.carregou.set(true);
      },
      error: () => this.carregou.set(true),
    });
  }
}

export function deslocar(competencia: string, meses: number): string {
  const [ano, mes] = competencia.split('-').map(Number);
  const data = new Date(ano, mes - 1 + meses, 1);
  return `${data.getFullYear()}-${String(data.getMonth() + 1).padStart(2, '0')}`;
}
