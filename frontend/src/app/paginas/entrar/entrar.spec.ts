import { HttpErrorResponse } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';
import { ActivatedRoute, Router, convertToParamMap, provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';

import { SessaoService } from '../../core/sessao.service';
import { Entrar } from './entrar';

describe('Entrar', () => {
  const sessao = {
    entrar: vi.fn(),
    demonstracao: vi.fn(),
  };

  function criar(voltar: string | null = null) {
    TestBed.configureTestingModule({
      imports: [Entrar],
      providers: [
        provideRouter([]),
        { provide: SessaoService, useValue: sessao },
        {
          provide: ActivatedRoute,
          useValue: { snapshot: { queryParamMap: convertToParamMap(voltar ? { voltar } : {}) } },
        },
      ],
    });
    const router = TestBed.inject(Router);
    vi.spyOn(router, 'navigateByUrl').mockResolvedValue(true);
    const fixture = TestBed.createComponent(Entrar);
    fixture.detectChanges();
    return { fixture, tela: fixture.componentInstance, router };
  }

  beforeEach(() => {
    sessao.entrar.mockReset();
    sessao.demonstracao.mockReturnValue(of({ ativa: false, email: null, senha: null }));
  });

  it('formulário vazio não chama a API', () => {
    const { tela } = criar();

    tela.entrar();

    expect(sessao.entrar).not.toHaveBeenCalled();
  });

  it('senha certa leva para a página que a pessoa tinha pedido', () => {
    sessao.entrar.mockReturnValue(of({ nome: 'Dona', email: 'dona@sirtech.example' }));
    const { tela, router } = criar('/notas');

    tela.form.setValue({ email: 'dona@sirtech.example', senha: 'segredo-123' });
    tela.entrar();

    expect(router.navigateByUrl).toHaveBeenCalledWith('/notas');
  });

  it('não segue um "voltar" que aponta para outro site', () => {
    sessao.entrar.mockReturnValue(of({ nome: 'Dona', email: 'dona@sirtech.example' }));
    const { tela, router } = criar('//site-malicioso.example');

    tela.form.setValue({ email: 'dona@sirtech.example', senha: 'segredo-123' });
    tela.entrar();

    expect(router.navigateByUrl).toHaveBeenCalledWith('/');
  });

  it('senha errada mostra a mensagem da API e limpa a senha', () => {
    sessao.entrar.mockReturnValue(
      throwError(
        () => new HttpErrorResponse({ status: 401, error: { detail: 'E-mail ou senha incorretos' } }),
      ),
    );
    const { tela, fixture } = criar();

    tela.form.setValue({ email: 'dona@sirtech.example', senha: 'errada-123' });
    tela.entrar();
    fixture.detectChanges();

    const alerta = (fixture.nativeElement as HTMLElement).querySelector('[role="alert"]');
    expect(alerta?.textContent).toContain('E-mail ou senha incorretos');
    expect(tela.form.controls.senha.value).toBe('');
    expect(tela.enviando()).toBe(false);
  });

  it('mostra o login de demonstração só quando a API oferece', () => {
    sessao.demonstracao.mockReturnValue(
      of({ ativa: true, email: 'demo@sirtech.example', senha: 'demo-sirtech-2026' }),
    );
    const { tela, fixture } = criar();

    expect((fixture.nativeElement as HTMLElement).querySelector('.demo')).not.toBeNull();
    tela.usarDemonstracao();
    expect(tela.form.getRawValue()).toEqual({ email: 'demo@sirtech.example', senha: 'demo-sirtech-2026' });
  });

  it('no uso real, a dica de demonstração não aparece', () => {
    const { fixture } = criar();

    expect((fixture.nativeElement as HTMLElement).querySelector('.demo')).toBeNull();
  });
});
