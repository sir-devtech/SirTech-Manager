import { CurrencyPipe } from '@angular/common';
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
  selector: 'app-emitir-dialog',
  imports: [
    ReactiveFormsModule,
    CurrencyPipe,
    MatDialogModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatDatepickerModule,
  ],
  providers: [provideNativeDateAdapter()],
  template: `
    <h2 mat-dialog-title>Emitir nota</h2>
    <form [formGroup]="form" (ngSubmit)="salvar()">
      <mat-dialog-content class="campos">
        <p>{{ nota.clienteNome }} · {{ nota.valor | currency: 'BRL' }}</p>
        <mat-form-field>
          <mat-label>Número</mat-label>
          <input matInput formControlName="numero" />
        </mat-form-field>
        <mat-form-field>
          <mat-label>Data de emissão</mat-label>
          <input matInput [matDatepicker]="picker" formControlName="dataEmissao" />
          <mat-datepicker-toggle matIconSuffix [for]="picker" />
          <mat-datepicker #picker />
        </mat-form-field>
      </mat-dialog-content>
      <mat-dialog-actions align="end">
        <button mat-button type="button" mat-dialog-close>Voltar</button>
        <button mat-flat-button color="primary" type="submit" [disabled]="salvando">Emitir</button>
      </mat-dialog-actions>
    </form>
  `,
})
export class EmitirDialog {
  private readonly fb = inject(FormBuilder);
  private readonly api = inject(NotaService);
  private readonly ref = inject(MatDialogRef<EmitirDialog, boolean>);
  readonly nota = inject<Nota>(MAT_DIALOG_DATA);
  salvando = false;

  readonly form = this.fb.nonNullable.group({
    numero: ['', Validators.required],
    dataEmissao: [new Date(), Validators.required],
  });

  salvar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const data = isoData(this.form.controls.dataEmissao.value);
    if (!data) {
      return;
    }
    this.salvando = true;
    this.api.emitir(this.nota.id, this.form.controls.numero.value.trim(), data).subscribe({
      next: () => this.ref.close(true),
      error: () => (this.salvando = false),
    });
  }
}
