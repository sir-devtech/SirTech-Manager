import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { Cliente, ClienteFormulario } from './modelos';

@Injectable({ providedIn: 'root' })
export class ClienteService {
  private readonly http = inject(HttpClient);

  listar(ativo?: boolean): Observable<Cliente[]> {
    let params = new HttpParams();
    if (ativo !== undefined) {
      params = params.set('ativo', ativo);
    }
    return this.http.get<Cliente[]>('/api/clientes', { params });
  }

  criar(dados: ClienteFormulario): Observable<Cliente> {
    return this.http.post<Cliente>('/api/clientes', dados);
  }

  atualizar(id: number, dados: ClienteFormulario): Observable<Cliente> {
    return this.http.put<Cliente>(`/api/clientes/${id}`, dados);
  }

  ativar(id: number): Observable<Cliente> {
    return this.http.patch<Cliente>(`/api/clientes/${id}/ativar`, {});
  }

  desativar(id: number): Observable<Cliente> {
    return this.http.patch<Cliente>(`/api/clientes/${id}/desativar`, {});
  }
}
