import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { Backup, Resumo } from './modelos';

@Injectable({ providedIn: 'root' })
export class ResumoService {
  private readonly http = inject(HttpClient);

  buscar(competencia: string): Observable<Resumo> {
    const params = new HttpParams().set('competencia', competencia);
    return this.http.get<Resumo>('/api/resumo', { params });
  }

  backup(): Observable<Backup> {
    return this.http.post<Backup>('/api/backup', {});
  }
}
