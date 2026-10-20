import { Component, input } from '@angular/core';

@Component({
  selector: 'app-leitura',
  template: `
    <aside>
      <p class="rotulo">Leitura rápida</p>
      <p class="resumo">{{ resumo() }}</p>
      <ul>
        <ng-content />
      </ul>
    </aside>
  `,
  styles: `
    aside {
      border-left: 2px solid var(--sir-cyan);
      padding: 2px 0 2px 14px;
    }

    .rotulo {
      margin: 0;
      font-size: 11px;
      letter-spacing: 0.08em;
      text-transform: uppercase;
      color: var(--sir-cyan);
    }

    .resumo {
      margin: 6px 0 10px;
      font-size: 15px;
      line-height: 1.35;
    }

    ul {
      margin: 0;
      padding: 0;
      list-style: none;
      display: flex;
      flex-direction: column;
      gap: 6px;
      color: var(--sir-muted);
      font-size: 13px;
      line-height: 1.4;
    }
  `,
})
export class LeituraRapida {
  readonly resumo = input('');
}
