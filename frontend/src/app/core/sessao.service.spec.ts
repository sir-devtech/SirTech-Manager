import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { SessaoService } from './sessao.service';

describe('SessaoService', () => {
  let sessao: SessaoService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    sessao = TestBed.inject(SessaoService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('com sessão aberta no servidor, guarda quem entrou', () => {
    let logado: boolean | undefined;
    sessao.conferir().subscribe((resposta) => (logado = resposta));

    http.expectOne('/api/acesso/sessao').flush({ nome: 'Dona', email: 'dona@sirtech.example' });

    expect(logado).toBe(true);
    expect(sessao.usuario()?.nome).toBe('Dona');
  });

  it('com 401, fica deslogado sem lançar erro', () => {
    let logado: boolean | undefined;
    sessao.conferir().subscribe((resposta) => (logado = resposta));

    http.expectOne('/api/acesso/sessao').flush(null, { status: 401, statusText: 'Unauthorized' });

    expect(logado).toBe(false);
    expect(sessao.logado()).toBe(false);
  });

  it('pergunta ao servidor uma vez só por carregamento', () => {
    sessao.conferir().subscribe();
    http.expectOne('/api/acesso/sessao').flush({ nome: 'Dona', email: 'dona@sirtech.example' });

    sessao.conferir().subscribe();
    http.expectNone('/api/acesso/sessao');
  });

  it('entrar manda e-mail e senha e marca como logado', () => {
    sessao.entrar('dona@sirtech.example', 'segredo-123').subscribe();

    const req = http.expectOne('/api/acesso/entrar');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ email: 'dona@sirtech.example', senha: 'segredo-123' });
    req.flush({ nome: 'Dona', email: 'dona@sirtech.example' });

    expect(sessao.logado()).toBe(true);
  });

  it('sair esquece quem estava logado', () => {
    sessao.entrar('dona@sirtech.example', 'segredo-123').subscribe();
    http.expectOne('/api/acesso/entrar').flush({ nome: 'Dona', email: 'dona@sirtech.example' });

    sessao.sair().subscribe();
    http.expectOne('/api/acesso/sair').flush(null, { status: 204, statusText: 'No Content' });

    expect(sessao.logado()).toBe(false);
  });

  it('se a dica de demonstração falhar, trata como desligada', () => {
    let ativa: boolean | undefined;
    sessao.demonstracao().subscribe((demo) => (ativa = demo.ativa));

    http.expectOne('/api/acesso/demonstracao').flush(null, { status: 500, statusText: 'Erro' });

    expect(ativa).toBe(false);
  });
});
