import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { Financeiro } from './modelos';

@Injectable({ providedIn: 'root' })
export class FinanceiroService {
  private readonly http = inject(HttpClient);

  consultar(inicio: string, fim: string): Observable<Financeiro> {
    const params = new HttpParams().set('inicio', inicio).set('fim', fim);
    return this.http.get<Financeiro>('/api/financeiro', { params });
  }
}
