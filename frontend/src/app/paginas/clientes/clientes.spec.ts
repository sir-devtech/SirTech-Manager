import { TestBed } from '@angular/core/testing';
import { MatDialog } from '@angular/material/dialog';
import { of } from 'rxjs';

import { ClienteService } from '../../core/cliente.service';
import { Cliente } from '../../core/modelos';
import { Clientes } from './clientes';

function cliente(id: number, nome: string, documento: string, extra: Partial<Cliente> = {}): Cliente {
  return {
    id,
    nome,
    documento,
    email: null,
    produto: 'SIR_BARBER',
    valorMensal: 300,
    diaVencimento: 10,
    ativo: true,
    criadoEm: '2026-10-01T09:00:00',
    ...extra,
  };
}

const BARBEARIA = cliente(1, 'Barbearia Navalha de Ouro (fictícia)', '11222333000181');
const PET = cliente(2, 'Pet Shop Amigo Fiel (fictícia)', '11444777000161', { produto: 'SIR_PET', valorMensal: 200 });
const PADARIA = cliente(3, 'Padaria Pão Quente (fictícia)', '12345678000195', { ativo: false });

describe('Clientes', () => {
  const api = { listar: vi.fn(), ativar: vi.fn(), desativar: vi.fn() };
  const dialog = { open: vi.fn() };

  function criar(lista: Cliente[] = [BARBEARIA, PET, PADARIA]) {
    api.listar.mockReturnValue(of(lista));
    TestBed.configureTestingModule({
      imports: [Clientes],
      providers: [
        { provide: ClienteService, useValue: api },
        { provide: MatDialog, useValue: dialog },
      ],
    });
    const fixture = TestBed.createComponent(Clientes);
    fixture.detectChanges();
    return { fixture, tela: fixture.componentInstance, html: fixture.nativeElement as HTMLElement };
  }

  function buscar(tela: Clientes, texto: string): void {
    tela.procurar({ target: { value: texto } } as unknown as Event);
  }

  function nomesNaTabela(html: HTMLElement): string[] {
    return Array.from(html.querySelectorAll('tr.mat-mdc-row td:first-child')).map(
      (celula) => celula.textContent?.trim() ?? '',
    );
  }

  beforeEach(() => {
    api.listar.mockReset();
    api.ativar.mockReset();
    api.desativar.mockReset();
    dialog.open.mockReset();
  });

  it('lista todos os clientes, ativos e inativos', () => {
    const { html } = criar();

    expect(nomesNaTabela(html)).toHaveLength(3);
  });

  it('mostra o documento com máscara, embora guarde só os dígitos', () => {
    const { html } = criar();

    expect(html.textContent).toContain('11.222.333/0001-81');
    expect(html.textContent).not.toContain('11222333000181');
  });

  it('a busca filtra pelo nome, sem ligar para maiúsculas', () => {
    const { tela, fixture, html } = criar();

    buscar(tela, 'NAVALHA');
    fixture.detectChanges();

    expect(nomesNaTabela(html)).toEqual(['Barbearia Navalha de Ouro (fictícia)']);
  });

  it('a busca acha pelo CNPJ digitado com máscara', () => {
    const { tela } = criar();

    buscar(tela, '11.444.777/0001-61');

    expect(tela.filtrados().map((c) => c.nome)).toEqual(['Pet Shop Amigo Fiel (fictícia)']);
  });

  it('a busca acha pelos dígitos soltos do CNPJ', () => {
    const { tela } = criar();

    buscar(tela, '12345678');

    expect(tela.filtrados().map((c) => c.id)).toEqual([3]);
  });

  it('busca em branco volta a mostrar todos', () => {
    const { tela } = criar();
    buscar(tela, 'navalha');

    buscar(tela, '   ');

    expect(tela.filtrados()).toHaveLength(3);
  });

  it('texto sem resultado mostra a frase de vazio da busca', () => {
    const { tela, fixture, html } = criar();

    buscar(tela, 'empresa que não existe');
    fixture.detectChanges();

    expect(html.querySelector('.vazio')?.textContent).toContain('Nenhuma empresa com esse nome.');
    expect(html.querySelector('table')).toBeNull();
  });

  it('sem nenhum cliente cadastrado, convida a cadastrar', () => {
    const { html } = criar([]);

    expect(html.querySelector('.vazio')?.textContent).toContain('Nenhum cliente ainda.');
  });

  it('a leitura rápida conta só os ativos, no singular e no plural', () => {
    const { tela } = criar();
    expect(tela.textoAtivos()).toBe('2 clientes ativos.');

    tela.clientes.set([BARBEARIA, PADARIA]);
    expect(tela.textoAtivos()).toBe('1 cliente ativo.');
  });

  it('o botão muda conforme a situação: ativo oferece Desativar, inativo oferece Ativar', () => {
    const { html } = criar();

    const botoes = Array.from(html.querySelectorAll('tr.mat-mdc-row')).map((linha) =>
      Array.from(linha.querySelectorAll('button')).map((b) => b.textContent?.trim()),
    );

    expect(botoes).toEqual([
      ['Editar', 'Desativar'],
      ['Editar', 'Desativar'],
      ['Editar', 'Ativar'],
    ]);
  });

  it('desativar chama a API de desativar e recarrega a lista', () => {
    api.desativar.mockReturnValue(of({ ...BARBEARIA, ativo: false }));
    const { tela } = criar();
    api.listar.mockClear();

    tela.alternar(BARBEARIA);

    expect(api.desativar).toHaveBeenCalledWith(1);
    expect(api.ativar).not.toHaveBeenCalled();
    expect(api.listar).toHaveBeenCalledTimes(1);
  });

  it('ativar um cliente inativo chama a API de ativar', () => {
    api.ativar.mockReturnValue(of({ ...PADARIA, ativo: true }));
    const { tela } = criar();

    tela.alternar(PADARIA);

    expect(api.ativar).toHaveBeenCalledWith(3);
    expect(api.desativar).not.toHaveBeenCalled();
  });

  it('depois de salvar na caixa de cadastro, a lista é recarregada', () => {
    dialog.open.mockReturnValue({ afterClosed: () => of(true) });
    const { tela } = criar();
    api.listar.mockClear();

    tela.abrir(null);

    expect(api.listar).toHaveBeenCalledTimes(1);
  });

  it('se a caixa for fechada sem salvar, não recarrega', () => {
    dialog.open.mockReturnValue({ afterClosed: () => of(undefined) });
    const { tela } = criar();
    api.listar.mockClear();

    tela.abrir(BARBEARIA);

    expect(api.listar).not.toHaveBeenCalled();
  });
});
