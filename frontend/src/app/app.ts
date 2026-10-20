import { Component, inject } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { MatButtonModule } from '@angular/material/button';
import { NavigationEnd, Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { filter, map, startWith } from 'rxjs';

import { CompetenciaService } from './core/competencia.service';
import { SessaoService } from './core/sessao.service';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, RouterLink, RouterLinkActive, MatButtonModule],
  templateUrl: './app.html',
  styleUrl: './app.scss',
})
export class App {
  private readonly router = inject(Router);
  readonly competencia = inject(CompetenciaService);
  readonly sessao = inject(SessaoService);

  private readonly url = toSignal(
    this.router.events.pipe(
      filter((evento): evento is NavigationEnd => evento instanceof NavigationEnd),
      map((evento) => evento.urlAfterRedirects),
      startWith(this.router.url),
    ),
    { initialValue: this.router.url },
  );

  readonly noFinanceiro = () => this.url().startsWith('/financeiro');

  trocarMes(evento: Event): void {
    this.competencia.definir((evento.target as HTMLInputElement).value);
  }

  sair(): void {
    this.sessao.sair().subscribe({
      complete: () => this.router.navigate(['/entrar']),
      error: () => this.router.navigate(['/entrar']),
    });
  }
}
