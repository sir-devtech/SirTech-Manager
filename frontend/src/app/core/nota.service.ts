import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { Nota, ResultadoGeracao } from './modelos';

@Injectable({ providedIn: 'root' })
export class NotaService {
  private readonly http = inject(HttpClient);

  listar(competencia: string, status?: string, clienteId?: number): Observable<Nota[]> {
    let params = new HttpParams().set('competencia', competencia);
    if (status) {
      params = params.set('status', status);
    }
    if (clienteId) {
      params = params.set('clienteId', clienteId);
    }
    return this.http.get<Nota[]>('/api/notas', { params });
  }

  emitir(id: number, numero: string, dataEmissao: string): Observable<Nota> {
    return this.http.patch<Nota>(`/api/notas/${id}/emitir`, { numero, dataEmissao });
  }

  pagar(id: number, dataPagamento: string): Observable<Nota> {
    return this.http.patch<Nota>(`/api/notas/${id}/pagar`, { dataPagamento });
  }

  cancelar(id: number): Observable<Nota> {
    return this.http.patch<Nota>(`/api/notas/${id}/cancelar`, {});
  }

  gerar(competencia: string): Observable<ResultadoGeracao> {
    const params = new HttpParams().set('competencia', competencia);
    return this.http.post<ResultadoGeracao>('/api/notas/gerar', null, { params });
  }

  importar(arquivo: File): Observable<Nota> {
    const corpo = new FormData();
    corpo.append('arquivo', arquivo);
    return this.http.post<Nota>('/api/notas/importar', corpo);
  }
}
