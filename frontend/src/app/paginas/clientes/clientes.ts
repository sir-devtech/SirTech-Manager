import { CurrencyPipe } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatPaginatorModule } from '@angular/material/paginator';
import { MatTableModule } from '@angular/material/table';

import { LeituraRapida } from '../../compartilhado/leitura-rapida';
import { ClienteService } from '../../core/cliente.service';
import { formatarDocumento } from '../../core/documento';
import { Cliente, PRODUTOS, rotuloDe } from '../../core/modelos';
import { Paginacao } from '../../core/paginacao';
import { ClienteDialog } from './cliente-dialog';

@Component({
  selector: 'app-clientes',
  imports: [MatTableModule, MatButtonModule, MatPaginatorModule, CurrencyPipe, LeituraRapida],
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
  readonly documento = formatarDocumento;
  readonly busca = signal('');
  readonly filtrados = computed(() => {
    const termo = this.busca().trim().toLowerCase();
    const digitos = termo.replace(/\D/g, '');
    if (!termo) {
      return this.clientes();
    }
    return this.clientes().filter((cliente) =>
      cliente.nome.toLowerCase().includes(termo) ||
      (digitos.length > 0 && cliente.documento.includes(digitos)));
  });

  textoAtivos(): string {
    const quantidade = this.clientes().filter((cliente) => cliente.ativo).length;
    return quantidade === 1 ? '1 cliente ativo.' : `${quantidade} clientes ativos.`;
  }

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

  procurar(evento: Event): void {
    this.busca.set((evento.target as HTMLInputElement).value);
    this.paginas.voltar();
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
