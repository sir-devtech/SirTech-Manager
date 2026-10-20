import { Component, inject } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';

export interface ConfirmaDados {
  titulo: string;
  texto: string;
}

@Component({
  selector: 'app-confirma',
  imports: [MatDialogModule, MatButtonModule],
  template: `
    <h2 mat-dialog-title>{{ dados.titulo }}</h2>
    <mat-dialog-content>{{ dados.texto }}</mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button mat-dialog-close>Voltar</button>
      <button mat-flat-button color="primary" [mat-dialog-close]="true">Confirmar</button>
    </mat-dialog-actions>
  `,
})
export class Confirma {
  readonly dados = inject<ConfirmaDados>(MAT_DIALOG_DATA);
}
