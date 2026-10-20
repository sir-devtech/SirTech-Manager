import { Routes } from '@angular/router';

import { sessaoGuard, visitanteGuard } from './core/sessao.guard';
import { Clientes } from './paginas/clientes/clientes';
import { Despesas } from './paginas/despesas/despesas';
import { Entrar } from './paginas/entrar/entrar';
import { Financeiro } from './paginas/financeiro/financeiro';
import { Notas } from './paginas/notas/notas';
import { Painel } from './paginas/painel/painel';

export const routes: Routes = [
  { path: 'entrar', component: Entrar, canActivate: [visitanteGuard], title: 'Entrar · Sir Tech' },
  {
    path: '',
    canActivateChild: [sessaoGuard],
    children: [
      { path: '', component: Painel, title: 'Painel · Sir Tech' },
      { path: 'financeiro', component: Financeiro, title: 'Financeiro · Sir Tech' },
      { path: 'clientes', component: Clientes, title: 'Clientes · Sir Tech' },
      { path: 'notas', component: Notas, title: 'Notas · Sir Tech' },
      { path: 'despesas', component: Despesas, title: 'Despesas · Sir Tech' },
    ],
  },
  { path: '**', redirectTo: '' },
];
