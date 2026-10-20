import { Routes } from '@angular/router';

import { Clientes } from './paginas/clientes/clientes';
import { Despesas } from './paginas/despesas/despesas';
import { Notas } from './paginas/notas/notas';
import { Financeiro } from './paginas/financeiro/financeiro';
import { Painel } from './paginas/painel/painel';

export const routes: Routes = [
  { path: '', component: Painel },
  { path: 'financeiro', component: Financeiro },
  { path: 'clientes', component: Clientes },
  { path: 'notas', component: Notas },
  { path: 'despesas', component: Despesas },
];
