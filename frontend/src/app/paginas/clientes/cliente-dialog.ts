import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';

import { ClienteService } from '../../core/cliente.service';
import { cpfOuCnpj } from '../../core/documento.validator';
import { Cliente, PRODUTOS } from '../../core/modelos';

@Component({
  selector: 'app-cliente-dialog',
  imports: [
    ReactiveFormsModule,
    MatDialogModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
  ],
  templateUrl: './cliente-dialog.html',
})
export class ClienteDialog {
  private readonly fb = inject(FormBuilder);
  private readonly api = inject(ClienteService);
  private readonly ref = inject(MatDialogRef<ClienteDialog, boolean>);
  readonly cliente = inject<Cliente | null>(MAT_DIALOG_DATA);
  readonly produtos = PRODUTOS;
  salvando = false;

  readonly form = this.fb.nonNullable.group({
    nome: [this.cliente?.nome ?? '', [Validators.required, Validators.maxLength(150)]],
    documento: [this.cliente?.documento ?? '', [Validators.required, cpfOuCnpj]],
    email: [this.cliente?.email ?? '', Validators.email],
    produto: [this.cliente?.produto ?? 'SIR_BARBER', Validators.required],
    valorMensal: [this.cliente?.valorMensal ?? 0, [Validators.required, Validators.min(0.01)]],
    diaVencimento: [
      this.cliente?.diaVencimento ?? 10,
      [Validators.required, Validators.min(1), Validators.max(28)],
    ],
  });

  salvar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const bruto = this.form.getRawValue();
    const dados = {
      nome: bruto.nome.trim(),
      documento: bruto.documento,
      email: bruto.email.trim() || null,
      produto: bruto.produto,
      valorMensal: Number(bruto.valorMensal),
      diaVencimento: Number(bruto.diaVencimento),
    };
    this.salvando = true;
    const pedido = this.cliente
      ? this.api.atualizar(this.cliente.id, dados)
      : this.api.criar(dados);
    pedido.subscribe({
      next: () => this.ref.close(true),
      error: () => (this.salvando = false),
    });
  }
}
