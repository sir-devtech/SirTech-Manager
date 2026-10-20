import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';

import { mensagemDe } from './mensagem-erro';
import { SessaoService } from './sessao.service';

/**
 * Erro da API vira aviso na tela. Duas exceções:
 * - as rotas de acesso: a própria tela de entrar mostra o erro dela;
 * - 401 em qualquer outra rota: a sessão expirou, então volta para o login sem aviso solto.
 */
export const erroApiInterceptor: HttpInterceptorFn = (req, next) => {
  const avisos = inject(MatSnackBar);
  const sessao = inject(SessaoService);
  const router = inject(Router);
  const rotaDeAcesso = req.url.startsWith('/api/acesso/');

  return next(req).pipe(
    catchError((erro: HttpErrorResponse) => {
      if (rotaDeAcesso) {
        return throwError(() => erro);
      }
      if (erro.status === 401) {
        sessao.esquecer();
        router.navigate(['/entrar'], { queryParams: { voltar: router.url } });
        return throwError(() => erro);
      }
      avisos.open(mensagemDe(erro), 'Fechar', { duration: 7000 });
      return throwError(() => erro);
    }),
  );
};
