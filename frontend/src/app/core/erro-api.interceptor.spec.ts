import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Router, provideRouter } from '@angular/router';

import { erroApiInterceptor } from './erro-api.interceptor';
import { SessaoService } from './sessao.service';

describe('erroApiInterceptor', () => {
  let http: HttpClient;
  let backend: HttpTestingController;
  const avisos = { open: vi.fn() };
  const sessao = { esquecer: vi.fn() };
  let router: Router;

  beforeEach(() => {
    avisos.open.mockReset();
    sessao.esquecer.mockReset();
    TestBed.configureTestingModule({
      providers: [
        provideRouter([]),
        provideHttpClient(withInterceptors([erroApiInterceptor])),
        provideHttpClientTesting(),
        { provide: MatSnackBar, useValue: avisos },
        { provide: SessaoService, useValue: sessao },
      ],
    });
    http = TestBed.inject(HttpClient);
    backend = TestBed.inject(HttpTestingController);
    router = TestBed.inject(Router);
    vi.spyOn(router, 'navigate').mockResolvedValue(true);
  });

  afterEach(() => backend.verify());

  it('regra de negócio vira aviso com o detalhe da API', () => {
    http.post('/api/notas/1/pagar', {}).subscribe({ error: () => undefined });

    backend
      .expectOne('/api/notas/1/pagar')
      .flush({ detail: 'Nota paga não pode ser alterada' }, { status: 422, statusText: 'Erro' });

    expect(avisos.open).toHaveBeenCalledWith('Nota paga não pode ser alterada', 'Fechar', {
      duration: 7000,
    });
  });

  it('401 fora do login esquece a sessão e volta para /entrar, sem aviso', () => {
    http.get('/api/clientes').subscribe({ error: () => undefined });

    backend.expectOne('/api/clientes').flush(null, { status: 401, statusText: 'Unauthorized' });

    expect(sessao.esquecer).toHaveBeenCalled();
    expect(router.navigate).toHaveBeenCalledWith(['/entrar'], expect.anything());
    expect(avisos.open).not.toHaveBeenCalled();
  });

  it('erro nas rotas de acesso fica para a própria tela de entrar', () => {
    http.post('/api/acesso/entrar', {}).subscribe({ error: () => undefined });

    backend
      .expectOne('/api/acesso/entrar')
      .flush({ detail: 'E-mail ou senha incorretos' }, { status: 401, statusText: 'Unauthorized' });

    expect(avisos.open).not.toHaveBeenCalled();
    expect(router.navigate).not.toHaveBeenCalled();
  });
});
