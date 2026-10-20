import { Component, inject } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { NavigationEnd, Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { filter, map, startWith } from 'rxjs';

import { CompetenciaService } from './core/competencia.service';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './app.html',
  styleUrl: './app.scss',
})
export class App {
  private readonly router = inject(Router);
  readonly competencia = inject(CompetenciaService);
  readonly noFinanceiro = toSignal(
    this.router.events.pipe(
      filter((evento): evento is NavigationEnd => evento instanceof NavigationEnd),
      map((evento) => evento.urlAfterRedirects.startsWith('/financeiro')),
      startWith(this.router.url.startsWith('/financeiro')),
    ),
    { initialValue: this.router.url.startsWith('/financeiro') },
  );

  trocarMes(evento: Event): void {
    this.competencia.definir((evento.target as HTMLInputElement).value);
  }
}
