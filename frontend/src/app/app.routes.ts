import { Routes } from '@angular/router';

import { Clientes } from './paginas/clientes/clientes';
import { Despesas } from './paginas/despesas/despesas';
import { Notas } from './paginas/notas/notas';
import { Painel } from './paginas/painel/painel';

export const routes: Routes = [
  { path: '', component: Painel },
  { path: 'clientes', component: Clientes },
  { path: 'notas', component: Notas },
  { path: 'despesas', component: Despesas },
];
