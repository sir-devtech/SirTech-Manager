import { CurrencyPipe, DatePipe } from '@angular/common';
import { Component, effect, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatPaginatorModule } from '@angular/material/paginator';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatTableModule } from '@angular/material/table';

import { Confirma } from '../../compartilhado/confirma';
import { ClienteService } from '../../core/cliente.service';
import { CompetenciaService } from '../../core/competencia.service';
import { Cliente, Nota, STATUS, StatusNota, rotuloDe } from '../../core/modelos';
import { NotaService } from '../../core/nota.service';
import { Paginacao } from '../../core/paginacao';
import { EmitirDialog } from './emitir-dialog';
import { PagarDialog } from './pagar-dialog';

@Component({
  selector: 'app-notas',
  imports: [
    MatTableModule,
    MatButtonModule,
    MatFormFieldModule,
    MatSelectModule,
    MatPaginatorModule,
    CurrencyPipe,
    DatePipe,
  ],
  templateUrl: './notas.html',
  styleUrl: './notas.scss',
})
export class Notas {
  private readonly api = inject(NotaService);
  private readonly clientesApi = inject(ClienteService);
  private readonly dialog = inject(MatDialog);
  private readonly avisos = inject(MatSnackBar);
  readonly competencia = inject(CompetenciaService);
  readonly notas = signal<Nota[]>([]);
  readonly carregou = signal(false);
  readonly clientes = signal<Cliente[]>([]);
  readonly status = signal<StatusNota | ''>('');
  readonly clienteId = signal<number | ''>('');
  readonly statusOpcoes = STATUS;
  readonly colunas = ['cliente', 'numero', 'valor', 'vencimento', 'status', 'acoes'];
  readonly paginas = new Paginacao();
  readonly rotuloStatus = (valor: StatusNota) => rotuloDe(STATUS, valor);

  constructor() {
    this.clientesApi.listar(true).subscribe((lista) => this.clientes.set(lista));
    effect(() => {
      this.competencia.valor();
      this.status();
      this.clienteId();
      this.carregar();
    });
  }

  escolherStatus(valor: StatusNota | ''): void {
    this.paginas.voltar();
    this.status.set(valor);
  }

  escolherCliente(valor: number | ''): void {
    this.paginas.voltar();
    this.clienteId.set(valor);
  }

  emitir(nota: Nota): void {
    this.dialog
      .open(EmitirDialog, { data: nota, width: '420px' })
      .afterClosed()
      .subscribe((ok) => ok && this.carregar());
  }

  pagar(nota: Nota): void {
    this.dialog
      .open(PagarDialog, { data: nota, width: '420px' })
      .afterClosed()
      .subscribe((ok) => ok && this.carregar());
  }

  cancelar(nota: Nota): void {
    this.dialog
      .open(Confirma, {
        data: {
          titulo: 'Cancelar nota',
          texto: `Cancelar a nota de ${nota.clienteNome} neste mês?`,
        },
      })
      .afterClosed()
      .subscribe((ok) => {
        if (ok) {
          this.api.cancelar(nota.id).subscribe(() => this.carregar());
        }
      });
  }

  importar(evento: Event): void {
    const arquivo = (evento.target as HTMLInputElement).files?.[0];
    if (!arquivo) {
      return;
    }
    this.api.importar(arquivo).subscribe((nota) => {
      this.avisos.open(`Nota ${nota.numero} importada para ${nota.clienteNome}.`, 'Fechar', {
        duration: 5000,
      });
      this.carregar();
    });
    (evento.target as HTMLInputElement).value = '';
  }

  private carregar(): void {
    const cliente = this.clienteId();
    this.api
      .listar(this.competencia.valor(), this.status() || undefined, cliente || undefined)
      .subscribe((lista) => {
        this.notas.set(lista);
        this.carregou.set(true);
      });
  }
}
