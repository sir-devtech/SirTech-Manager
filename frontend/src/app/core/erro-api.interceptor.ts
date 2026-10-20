import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { catchError, throwError } from 'rxjs';

import { mensagemDe } from './mensagem-erro';

export const erroApiInterceptor: HttpInterceptorFn = (req, next) => {
  const avisos = inject(MatSnackBar);
  return next(req).pipe(
    catchError((erro: HttpErrorResponse) => {
      avisos.open(mensagemDe(erro), 'Fechar', { duration: 7000 });
      return throwError(() => erro);
    }),
  );
};
