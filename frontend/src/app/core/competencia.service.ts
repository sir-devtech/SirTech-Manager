import { Injectable, signal } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class CompetenciaService {
  private readonly atual = signal(mesAtual());
  readonly valor = this.atual.asReadonly();

  definir(valor: string): void {
    if (/^\d{4}-\d{2}$/.test(valor)) {
      this.atual.set(valor);
    }
  }
}

function mesAtual(): string {
  const texto = new Intl.DateTimeFormat('en-CA', {
    timeZone: 'America/Sao_Paulo',
    year: 'numeric',
    month: '2-digit',
  }).format(new Date());
  return texto.slice(0, 7);
}
