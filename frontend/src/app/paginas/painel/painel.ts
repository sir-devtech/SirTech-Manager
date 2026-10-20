import { CurrencyPipe, DatePipe, PercentPipe } from '@angular/common';
import { Component, computed, effect, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatPaginatorModule } from '@angular/material/paginator';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatTableModule } from '@angular/material/table';
import { forkJoin } from 'rxjs';

import { LeituraRapida } from '../../compartilhado/leitura-rapida';
import { CompetenciaService } from '../../core/competencia.service';
import { Nota, Resumo } from '../../core/modelos';
import { NotaService } from '../../core/nota.service';
import { Paginacao } from '../../core/paginacao';
import { ResumoService } from '../../core/resumo.service';

const MESES_NO_GRAFICO = 6;

export interface MesDoGrafico {
  resumo: Resumo;
  rotulo: string;
  atual: boolean;
}

@Component({
  selector: 'app-painel',
  imports: [
    MatButtonModule,
    MatIconModule,
    MatTableModule,
    MatPaginatorModule,
    LeituraRapida,
    CurrencyPipe,
    DatePipe,
    PercentPipe,
  ],
  templateUrl: './painel.html',
  styleUrl: './painel.scss',
})
export class Painel {
  private readonly resumoApi = inject(ResumoService);
  private readonly notasApi = inject(NotaService);
  private readonly avisos = inject(MatSnackBar);
  readonly competencia = inject(CompetenciaService);
  readonly resumo = signal<Resumo | null>(null);
  readonly historico = signal<MesDoGrafico[]>([]);
  readonly pendentes = signal<Nota[]>([]);
  readonly vencidas = signal<Nota[]>([]);
  readonly paginasPendentes = new Paginacao();
  readonly paginasVencidas = new Paginacao();
  readonly carregou = signal(false);

  readonly fracaoRecebida = computed(() => {
    const resumo = this.resumo();
    if (!resumo || resumo.receitaPrevista <= 0) {
      return 0;
    }
    return Math.min(resumo.receitaRecebida / resumo.receitaPrevista, 1);
  });

  readonly totalVencido = computed(() =>
    this.vencidas().reduce((soma, nota) => soma + nota.valor, 0),
  );

  readonly escalaDoGrafico = computed(() =>
    Math.max(
      1,
      ...this.historico().flatMap(({ resumo }) => [
        resumo.receitaPrevista,
        resumo.receitaRecebida,
        resumo.totalDespesas,
      ]),
    ),
  );

  constructor() {
    effect(() => this.carregar(this.competencia.valor()));
  }

  altura(valor: number): string {
    return `${(valor / this.escalaDoGrafico()) * 100}%`;
  }

  diasDeAtraso(nota: Nota): number {
    const vencimento = new Date(`${nota.dataVencimento}T00:00:00`);
    const hoje = new Date();
    hoje.setHours(0, 0, 0, 0);
    return Math.round((hoje.getTime() - vencimento.getTime()) / 86_400_000);
  }

  textoPendentes(): string {
    const quantidade = this.resumo()?.notasPendentesEmissao ?? 0;
    return quantidade === 1 ? '1 nota ainda por emitir.' : `${quantidade} notas ainda por emitir.`;
  }

  gerar(): void {
    this.notasApi.gerar(this.competencia.valor()).subscribe((resultado) => {
      this.avisos.open(
        `Notas novas: ${resultado.notasCriadas}. Despesas copiadas: ${resultado.despesasReplicadas}.`,
        'Fechar',
        { duration: 5000 },
      );
      this.carregar(this.competencia.valor());
    });
  }

  backup(): void {
    this.resumoApi.backup().subscribe((arquivo) => {
      this.avisos.open(`Backup gravado: ${arquivo.arquivo}`, 'Fechar', { duration: 6000 });
    });
  }

  private carregar(mes: string): void {
    const meses = mesesAte(mes, MESES_NO_GRAFICO);
    forkJoin(meses.map((competencia) => this.resumoApi.buscar(competencia))).subscribe(
      (resumos) => {
        this.resumo.set(resumos[resumos.length - 1]);
        this.historico.set(
          resumos.map((resumo, i) => ({
            resumo,
            rotulo: rotuloDoMes(meses[i]),
            atual: i === resumos.length - 1,
          })),
        );
      },
    );
    this.notasApi.listar(mes).subscribe((notas) => {
      this.pendentes.set(notas.filter((nota) => nota.status === 'PENDENTE'));
      this.vencidas.set(notas.filter((nota) => nota.vencida));
      this.paginasPendentes.voltar();
      this.paginasVencidas.voltar();
      this.carregou.set(true);
    });
  }
}

function mesesAte(competencia: string, quantidade: number): string[] {
  const [ano, mes] = competencia.split('-').map(Number);
  return Array.from({ length: quantidade }, (_, i) => {
    const data = new Date(ano, mes - 1 - (quantidade - 1 - i), 1);
    return `${data.getFullYear()}-${String(data.getMonth() + 1).padStart(2, '0')}`;
  });
}

function rotuloDoMes(competencia: string): string {
  const [ano, mes] = competencia.split('-').map(Number);
  return new Intl.DateTimeFormat('pt-BR', { month: 'short' })
    .format(new Date(ano, mes - 1, 1))
    .replace('.', '');
}
