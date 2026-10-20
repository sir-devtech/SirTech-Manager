import { HttpErrorResponse } from '@angular/common/http';

export function mensagemDe(erro: HttpErrorResponse): string {
  const corpo = erro.error as { detail?: string; erros?: Record<string, string> } | null;
  if (corpo && typeof corpo === 'object') {
    const campos = corpo.erros;
    if (campos && Object.keys(campos).length > 0) {
      return Object.entries(campos)
        .map(([campo, texto]) => `${campo}: ${texto}`)
        .join(' ');
    }
    if (corpo.detail) {
      return corpo.detail;
    }
  }
  return 'Não foi possível completar esta ação.';
}
