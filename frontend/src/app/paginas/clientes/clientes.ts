import { CurrencyPipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatPaginatorModule } from '@angular/material/paginator';
import { MatTableModule } from '@angular/material/table';

import { ClienteService } from '../../core/cliente.service';
import { Cliente, PRODUTOS, rotuloDe } from '../../core/modelos';
import { Paginacao } from '../../core/paginacao';
import { ClienteDialog } from './cliente-dialog';

@Component({
  selector: 'app-clientes',
  imports: [MatTableModule, MatButtonModule, MatPaginatorModule, CurrencyPipe],
  templateUrl: './clientes.html',
  styleUrl: './clientes.scss',
})
export class Clientes {
  private readonly api = inject(ClienteService);
  private readonly dialog = inject(MatDialog);
  readonly clientes = signal<Cliente[]>([]);
  readonly carregou = signal(false);
  readonly colunas = ['nome', 'documento', 'produto', 'valor', 'vencimento', 'ativo', 'acoes'];
  readonly paginas = new Paginacao();
  readonly rotuloProduto = (produto: Cliente['produto']) => rotuloDe(PRODUTOS, produto);

  constructor() {
    this.carregar();
  }

  abrir(cliente: Cliente | null): void {
    this.dialog
      .open(ClienteDialog, { data: cliente, width: '440px' })
      .afterClosed()
      .subscribe((salvou) => {
        if (salvou) {
          this.carregar();
        }
      });
  }

  alternar(cliente: Cliente): void {
    const pedido = cliente.ativo ? this.api.desativar(cliente.id) : this.api.ativar(cliente.id);
    pedido.subscribe(() => this.carregar());
  }

  private carregar(): void {
    this.api.listar().subscribe((lista) => {
      this.clientes.set(lista);
      this.carregou.set(true);
    });
  }
}
