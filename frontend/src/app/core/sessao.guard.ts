import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { map } from 'rxjs';

import { SessaoService } from './sessao.service';

/** Sem sessão, qualquer página manda para /entrar, lembrando para onde a pessoa queria ir. */
export const sessaoGuard: CanActivateFn = (_rota, estado) => {
  const sessao = inject(SessaoService);
  const router = inject(Router);
  return sessao
    .conferir()
    .pipe(
      map((logado) =>
        logado ? true : router.createUrlTree(['/entrar'], { queryParams: { voltar: estado.url } }),
      ),
    );
};

/** Quem já entrou e abre /entrar vai direto para o painel. */
export const visitanteGuard: CanActivateFn = () => {
  const sessao = inject(SessaoService);
  const router = inject(Router);
  return sessao.conferir().pipe(map((logado) => (logado ? router.createUrlTree(['/']) : true)));
};
