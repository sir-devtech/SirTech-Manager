import { Component, inject } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';

import { CompetenciaService } from './core/competencia.service';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './app.html',
  styleUrl: './app.scss',
})
export class App {
  readonly competencia = inject(CompetenciaService);

  trocarMes(evento: Event): void {
    this.competencia.definir((evento.target as HTMLInputElement).value);
  }
}
