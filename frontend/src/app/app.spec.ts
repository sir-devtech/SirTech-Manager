import { computed, signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';

import { App } from './app';
import { UsuarioLogado } from './core/modelos';
import { SessaoService } from './core/sessao.service';

describe('App', () => {
  const usuario = signal<UsuarioLogado | null>(null);

  beforeEach(async () => {
    usuario.set(null);
    await TestBed.configureTestingModule({
      imports: [App],
      providers: [
        provideRouter([]),
        {
          provide: SessaoService,
          useValue: { usuario, logado: computed(() => usuario() !== null), sair: vi.fn() },
        },
      ],
    }).compileComponents();
  });

  it('sem login, não mostra o menu', async () => {
    const fixture = TestBed.createComponent(App);
    await fixture.whenStable();

    expect((fixture.nativeElement as HTMLElement).querySelector('nav')).toBeNull();
  });

  it('logado, mostra a marca, o menu e quem entrou', async () => {
    usuario.set({ nome: 'Dona da Sir Tech', email: 'dona@sirtech.example' });
    const fixture = TestBed.createComponent(App);
    await fixture.whenStable();
    fixture.detectChanges();

    const tela = fixture.nativeElement as HTMLElement;
    expect(tela.querySelector('.marca img')?.getAttribute('alt')).toContain('Sir Tech');
    expect(tela.querySelectorAll('nav a').length).toBe(5);
    expect(tela.querySelector('.quem')?.textContent).toContain('Dona da Sir Tech');
  });
});
