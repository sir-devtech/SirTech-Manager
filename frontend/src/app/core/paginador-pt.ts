import { Injectable } from '@angular/core';
import { MatPaginatorIntl } from '@angular/material/paginator';

@Injectable()
export class PaginadorPt extends MatPaginatorIntl {
  override itemsPerPageLabel = 'Por página';
  override nextPageLabel = 'Próxima';
  override previousPageLabel = 'Anterior';
  override firstPageLabel = 'Primeira';
  override lastPageLabel = 'Última';

  override getRangeLabel = (pagina: number, tamanho: number, total: number): string => {
    if (total === 0 || tamanho === 0) {
      return '0 de 0';
    }
    const inicio = pagina * tamanho;
    const fim = Math.min(inicio + tamanho, total);
    return `${inicio + 1}–${fim} de ${total}`;
  };
}
