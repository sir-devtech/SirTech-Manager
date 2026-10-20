import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { ActivatedRoute, Router } from '@angular/router';

import { mensagemDe } from '../../core/mensagem-erro';
import { SessaoService } from '../../core/sessao.service';

@Component({
  selector: 'app-entrar',
  imports: [ReactiveFormsModule, MatButtonModule, MatFormFieldModule, MatInputModule],
  templateUrl: './entrar.html',
  styleUrl: './entrar.scss',
})
export class Entrar {
  private readonly fb = inject(FormBuilder);
  private readonly sessao = inject(SessaoService);
  private readonly router = inject(Router);
  private readonly rota = inject(ActivatedRoute);

  readonly enviando = signal(false);
  readonly erro = signal<string | null>(null);
  readonly senhaVisivel = signal(false);
  readonly demonstracao = toSignal(this.sessao.demonstracao());

  readonly form = this.fb.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    senha: ['', Validators.required],
  });

  entrar(): void {
    if (this.form.invalid || this.enviando()) {
      this.form.markAllAsTouched();
      return;
    }
    this.enviando.set(true);
    this.erro.set(null);
    const { email, senha } = this.form.getRawValue();
    this.sessao.entrar(email, senha).subscribe({
      next: () => this.router.navigateByUrl(this.destino()),
      error: (falha: HttpErrorResponse) => {
        this.enviando.set(false);
        this.form.controls.senha.reset();
        this.erro.set(mensagemDe(falha));
      },
    });
  }

  /** Preenche o formulário com o login de demonstração, quando a API oferece um. */
  usarDemonstracao(): void {
    const demo = this.demonstracao();
    if (demo?.ativa && demo.email && demo.senha) {
      this.form.setValue({ email: demo.email, senha: demo.senha });
    }
  }

  /** Volta para a página que a pessoa tentou abrir, mas só se for um caminho interno. */
  private destino(): string {
    const voltar = this.rota.snapshot.queryParamMap.get('voltar');
    return voltar && voltar.startsWith('/') && !voltar.startsWith('//') ? voltar : '/';
  }
}
