import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { Despesa, DespesaFormulario } from './modelos';

@Injectable({ providedIn: 'root' })
export class DespesaService {
  private readonly http = inject(HttpClient);

  listar(competencia: string): Observable<Despesa[]> {
    const params = new HttpParams().set('competencia', competencia);
    return this.http.get<Despesa[]>('/api/despesas', { params });
  }

  criar(dados: DespesaFormulario): Observable<Despesa> {
    return this.http.post<Despesa>('/api/despesas', dados);
  }

  atualizar(id: number, dados: DespesaFormulario): Observable<Despesa> {
    return this.http.put<Despesa>(`/api/despesas/${id}`, dados);
  }

  excluir(id: number): Observable<void> {
    return this.http.delete<void>(`/api/despesas/${id}`);
  }
}
