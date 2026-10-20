import { signal } from '@angular/core';
import { PageEvent } from '@angular/material/paginator';

/** Corte da lista na tela. O volume da Sir Tech cabe numa página; o controle fica para crescer. */
export class Paginacao {
  readonly indice = signal(0);
  readonly tamanho = signal(5);
  readonly opcoes = [5, 10, 20];

  fatia<T>(lista: readonly T[]): T[] {
    const inicio = this.indice() * this.tamanho();
    return lista.slice(inicio, inicio + this.tamanho());
  }

  mudar(evento: PageEvent): void {
    this.indice.set(evento.pageIndex);
    this.tamanho.set(evento.pageSize);
  }

  voltar(): void {
    this.indice.set(0);
  }
}
