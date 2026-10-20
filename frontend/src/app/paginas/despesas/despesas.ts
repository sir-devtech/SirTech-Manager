import { CurrencyPipe } from '@angular/common';
import { Component, effect, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatPaginatorModule } from '@angular/material/paginator';
import { MatTableModule } from '@angular/material/table';

import { Confirma } from '../../compartilhado/confirma';
import { CompetenciaService } from '../../core/competencia.service';
import { DespesaService } from '../../core/despesa.service';
import { CATEGORIAS, Despesa, rotuloDe } from '../../core/modelos';
import { Paginacao } from '../../core/paginacao';
import { DespesaDialog } from './despesa-dialog';

@Component({
  selector: 'app-despesas',
  imports: [MatTableModule, MatButtonModule, MatPaginatorModule, CurrencyPipe],
  templateUrl: './despesas.html',
  styleUrl: './despesas.scss',
})
export class Despesas {
  private readonly api = inject(DespesaService);
  private readonly dialog = inject(MatDialog);
  readonly competencia = inject(CompetenciaService);
  readonly despesas = signal<Despesa[]>([]);
  readonly carregou = signal(false);
  readonly colunas = ['descricao', 'categoria', 'valor', 'recorrente', 'acoes'];
  readonly paginas = new Paginacao();
  readonly rotulo = (categoria: Despesa['categoria']) => rotuloDe(CATEGORIAS, categoria);

  constructor() {
    effect(() => {
      this.competencia.valor();
      this.carregar();
    });
  }

  abrir(despesa: Despesa | null): void {
    this.dialog
      .open(DespesaDialog, {
        data: { despesa, competencia: this.competencia.valor() },
        width: '440px',
      })
      .afterClosed()
      .subscribe((ok) => ok && this.carregar());
  }

  excluir(despesa: Despesa): void {
    this.dialog
      .open(Confirma, {
        data: { titulo: 'Excluir despesa', texto: `Excluir “${despesa.descricao}”?` },
      })
      .afterClosed()
      .subscribe((ok) => {
        if (ok) {
          this.api.excluir(despesa.id).subscribe(() => this.carregar());
        }
      });
  }

  private carregar(): void {
    this.api.listar(this.competencia.valor()).subscribe((lista) => {
      this.despesas.set(lista);
      this.carregou.set(true);
    });
  }
}
