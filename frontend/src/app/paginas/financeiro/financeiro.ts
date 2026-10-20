import { CurrencyPipe, PercentPipe } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';

import { LeituraRapida } from '../../compartilhado/leitura-rapida';
import { CompetenciaService } from '../../core/competencia.service';
import { FinanceiroService } from '../../core/financeiro.service';
import {
  CATEGORIAS,
  CategoriaDespesa,
  Fatia,
  Financeiro as Visao,
  PRODUTOS,
  Produto,
  rotuloDe,
} from '../../core/modelos';

type Atalho = 'mes' | 'anterior' | 'ano' | 'intervalo';

@Component({
  selector: 'app-financeiro',
  imports: [CurrencyPipe, PercentPipe, MatIconModule, LeituraRapida],
  templateUrl: './financeiro.html',
  styleUrl: './financeiro.scss',
})
export class Financeiro {
  private readonly api = inject(FinanceiroService);
  private readonly competencia = inject(CompetenciaService);

  readonly atalho = signal<Atalho>('mes');
  readonly inicio = signal(this.competencia.valor());
  readonly fim = signal(this.competencia.valor());
  readonly dados = signal<Visao | null>(null);
  readonly carregou = signal(false);

  readonly concentrado = computed(() =>
    (this.dados()?.porCliente ?? []).some((fatia) => fatia.parte > 0.5),
  );

  readonly escala = computed(() =>
    Math.max(
      1,
      ...(this.dados()?.meses ?? []).flatMap((mes) => [
        mes.receitaPrevista,
        mes.receitaRecebida,
        mes.totalDespesas,
      ]),
    ),
  );

  constructor() {
    this.buscar();
  }

  escolher(atalho: Atalho): void {
    const mes = this.competencia.valor();
    this.atalho.set(atalho);
    if (atalho === 'mes') {
      this.definir(mes, mes);
    } else if (atalho === 'anterior') {
      const anterior = deslocar(mes, -1);
      this.definir(anterior, anterior);
    } else if (atalho === 'ano') {
      const ano = mes.slice(0, 4);
      this.definir(`${ano}-01`, `${ano}-12`);
    }
  }

  aplicarIntervalo(): void {
    if (this.inicio() > this.fim()) {
      return;
    }
    this.atalho.set('intervalo');
    this.buscar();
  }

  textoLeitura(): string {
    return this.concentrado()
      ? 'Um cliente passa de metade da receita.'
      : 'Nenhum cliente passa de metade da receita.';
  }

  semAtraso(dados: Visao): boolean {
    const atraso = dados.atraso;
    return atraso.aVencer + atraso.ate30 + atraso.de31a60 + atraso.acimaDe60 === 0;
  }

  altura(valor: number): string {
    return `${(valor / this.escala()) * 100}%`;
  }

  rotuloMes(competencia: string): string {
    const [ano, mes] = competencia.split('-').map(Number);
    return new Intl.DateTimeFormat('pt-BR', { month: 'short' })
      .format(new Date(ano, mes - 1, 1))
      .replace('.', '');
  }

  nomeCliente(fatia: Fatia): string {
    return fatia.nome;
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
    this.dados.set(null);
    this.api.consultar(this.inicio(), this.fim()).subscribe({
      next: (valor) => {
        this.dados.set(valor);
        this.carregou.set(true);
      },
      error: () => this.carregou.set(true),
    });
  }
}

function deslocar(competencia: string, meses: number): string {
  const [ano, mes] = competencia.split('-').map(Number);
  const data = new Date(ano, mes - 1 + meses, 1);
  return `${data.getFullYear()}-${String(data.getMonth() + 1).padStart(2, '0')}`;
}
