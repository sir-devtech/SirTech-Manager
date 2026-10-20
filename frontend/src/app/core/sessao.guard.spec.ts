import { TestBed } from '@angular/core/testing';
import { ActivatedRouteSnapshot, Router, RouterStateSnapshot, UrlTree, provideRouter } from '@angular/router';
import { Observable, firstValueFrom, of } from 'rxjs';

import { sessaoGuard, visitanteGuard } from './sessao.guard';
import { SessaoService } from './sessao.service';

describe('guardas de sessão', () => {
  function preparar(logado: boolean): Router {
    TestBed.configureTestingModule({
      providers: [
        provideRouter([]),
        { provide: SessaoService, useValue: { conferir: () => of(logado) } },
      ],
    });
    return TestBed.inject(Router);
  }

  function rodar(guarda: typeof sessaoGuard, url: string) {
    const estado = { url } as RouterStateSnapshot;
    return TestBed.runInInjectionContext(() =>
      guarda({} as ActivatedRouteSnapshot, estado),
    ) as Observable<boolean | UrlTree>;
  }

  it('sem login, manda para /entrar lembrando a página pedida', async () => {
    const router = preparar(false);

    const resultado = await firstValueFrom(rodar(sessaoGuard, '/notas'));

    expect(router.serializeUrl(resultado as UrlTree)).toBe('/entrar?voltar=%2Fnotas');
  });

  it('com login, deixa abrir a página', async () => {
    preparar(true);

    expect(await firstValueFrom(rodar(sessaoGuard, '/notas'))).toBe(true);
  });

  it('quem já entrou e abre /entrar vai para o painel', async () => {
    const router = preparar(true);

    const resultado = await firstValueFrom(rodar(visitanteGuard, '/entrar'));

    expect(router.serializeUrl(resultado as UrlTree)).toBe('/');
  });
});
