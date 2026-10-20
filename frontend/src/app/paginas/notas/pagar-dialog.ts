import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { provideNativeDateAdapter } from '@angular/material/core';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';

import { isoData } from '../../core/data';
import { Nota } from '../../core/modelos';
import { NotaService } from '../../core/nota.service';

@Component({
  selector: 'app-pagar-dialog',
  imports: [
    ReactiveFormsModule,
    MatDialogModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatDatepickerModule,
  ],
  providers: [provideNativeDateAdapter()],
  template: `
    <h2 mat-dialog-title>Registrar pagamento</h2>
    <form [formGroup]="form" (ngSubmit)="salvar()">
      <mat-dialog-content class="campos">
        <p>{{ nota.clienteNome }} · nota {{ nota.numero }}</p>
        <mat-form-field>
          <mat-label>Data do pagamento</mat-label>
          <input matInput [matDatepicker]="picker" formControlName="dataPagamento" />
          <mat-datepicker-toggle matIconSuffix [for]="picker" />
          <mat-datepicker #picker />
        </mat-form-field>
      </mat-dialog-content>
      <mat-dialog-actions align="end">
        <button mat-button type="button" mat-dialog-close>Voltar</button>
        <button mat-flat-button color="primary" type="submit" [disabled]="salvando">Pagar</button>
      </mat-dialog-actions>
    </form>
  `,
})
export class PagarDialog {
  private readonly fb = inject(FormBuilder);
  private readonly api = inject(NotaService);
  private readonly ref = inject(MatDialogRef<PagarDialog, boolean>);
  readonly nota = inject<Nota>(MAT_DIALOG_DATA);
  salvando = false;

  readonly form = this.fb.nonNullable.group({
    dataPagamento: [new Date(), Validators.required],
  });

  salvar(): void {
    const data = isoData(this.form.controls.dataPagamento.value);
    if (!data) {
      return;
    }
    this.salvando = true;
    this.api.pagar(this.nota.id, data).subscribe({
      next: () => this.ref.close(true),
      error: () => (this.salvando = false),
    });
  }
}
