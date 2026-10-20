import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';

import { DespesaService } from '../../core/despesa.service';
import { CATEGORIAS, Despesa } from '../../core/modelos';

export interface DespesaDialogDados {
  despesa: Despesa | null;
  competencia: string;
}

@Component({
  selector: 'app-despesa-dialog',
  imports: [
    ReactiveFormsModule,
    MatDialogModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatCheckboxModule,
  ],
  templateUrl: './despesa-dialog.html',
})
export class DespesaDialog {
  private readonly fb = inject(FormBuilder);
  private readonly api = inject(DespesaService);
  private readonly ref = inject(MatDialogRef<DespesaDialog, boolean>);
  readonly dados = inject<DespesaDialogDados>(MAT_DIALOG_DATA);
  readonly categorias = CATEGORIAS;
  salvando = false;

  readonly form = this.fb.nonNullable.group({
    descricao: [this.dados.despesa?.descricao ?? '', [Validators.required, Validators.maxLength(150)]],
    categoria: [this.dados.despesa?.categoria ?? 'SERVIDOR', Validators.required],
    valor: [this.dados.despesa?.valor ?? 150, [Validators.required, Validators.min(0.01)]],
    recorrente: [this.dados.despesa?.recorrente ?? true],
  });

  salvar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const bruto = this.form.getRawValue();
    const corpo = {
      descricao: bruto.descricao.trim(),
      categoria: bruto.categoria,
      valor: Number(bruto.valor),
      competencia: this.dados.competencia,
      recorrente: bruto.recorrente,
    };
    this.salvando = true;
    const pedido = this.dados.despesa
      ? this.api.atualizar(this.dados.despesa.id, corpo)
      : this.api.criar(corpo);
    pedido.subscribe({
      next: () => this.ref.close(true),
      error: () => (this.salvando = false),
    });
  }
}
