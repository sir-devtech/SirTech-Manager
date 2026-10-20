import { HttpClient } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { Observable, catchError, map, of, tap } from 'rxjs';

import { Demonstracao, UsuarioLogado } from './modelos';

/**
 * Quem está logado. A sessão de verdade mora no servidor, num cookie HttpOnly que o JavaScript
 * não lê. Aqui fica só o nome para mostrar no topo e saber se a tela pode abrir.
 */
@Injectable({ providedIn: 'root' })
export class SessaoService {
  private readonly http = inject(HttpClient);
  private readonly atual = signal<UsuarioLogado | null>(null);
  private conferida = false;

  readonly usuario = this.atual.asReadonly();
  readonly logado = computed(() => this.atual() !== null);

  /**
   * Pergunta ao servidor uma vez por carregamento da página. Também é esta chamada que traz o
   * cookie XSRF-TOKEN, que o Angular devolve sozinho no POST de entrar.
   */
  conferir(): Observable<boolean> {
    if (this.conferida) {
      return of(this.logado());
    }
    return this.http.get<UsuarioLogado>('/api/acesso/sessao').pipe(
      tap((usuario) => this.atual.set(usuario)),
      map(() => true),
      catchError(() => {
        this.atual.set(null);
        return of(false);
      }),
      tap(() => (this.conferida = true)),
    );
  }

  entrar(email: string, senha: string): Observable<UsuarioLogado> {
    return this.http
      .post<UsuarioLogado>('/api/acesso/entrar', { email, senha })
      .pipe(tap((usuario) => this.marcarLogado(usuario)));
  }

  /** Login de exemplo para a tela de entrar. A API só preenche no ambiente de demonstração. */
  demonstracao(): Observable<Demonstracao> {
    return this.http
      .get<Demonstracao>('/api/acesso/demonstracao')
      .pipe(catchError(() => of({ ativa: false, email: null, senha: null })));
  }

  sair(): Observable<void> {
    return this.http.post<void>('/api/acesso/sair', {}).pipe(tap(() => this.esquecer()));
  }

  /** Chamado quando a API responde 401: a sessão expirou no servidor. */
  esquecer(): void {
    this.atual.set(null);
    this.conferida = true;
  }

  private marcarLogado(usuario: UsuarioLogado): void {
    this.atual.set(usuario);
    this.conferida = true;
  }
}
