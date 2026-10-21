import { Page, expect } from '@playwright/test';

export const LOGIN_DEMONSTRACAO = { email: 'demo@sirtech.example', senha: 'demo-sirtech-2026' };

/** Entra do jeito que uma pessoa entra: abre a tela, clica em Preencher e em Entrar. */
export async function entrarComDemonstracao(page: Page): Promise<void> {
  await page.goto('/');
  await expect(page).toHaveURL(/\/entrar/);
  await page.getByRole('button', { name: 'Preencher' }).click();
  await page.getByRole('button', { name: 'Entrar', exact: true }).click();
  await expect(page.getByRole('heading', { name: 'Painel', level: 1 })).toBeVisible();
}

/** Linha da tabela de um cliente, achada pelo nome (a ordem das linhas pode mudar). */
export function linhaDe(page: Page, nome: string) {
  return page.locator('tr.mat-mdc-row', { hasText: nome });
}
