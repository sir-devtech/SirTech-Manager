import { Page, expect, test } from '@playwright/test';

import { linhaDe } from '../tests/ajudas';

// Onde salvar: scripts/e2e.sh monta docs/screenshots em /saida.
const SAIDA = process.env.SAIDA ?? 'saida';

/** Pausa curta para quem assiste ao vídeo conseguir ler a tela. */
const pausa = (page: Page, ms = 900) => page.waitForTimeout(ms);

async function foto(page: Page, nome: string, paginaInteira = false): Promise<void> {
  await page.waitForLoadState('networkidle');
  await pausa(page, 600);
  await page.screenshot({ path: `${SAIDA}/${nome}.png`, fullPage: paginaInteira });
}

test('percorre o sistema, tira os prints e grava o vídeo', async ({ page }) => {
  // 1. Tela de entrar, com o login de demonstração.
  await page.goto('/');
  await expect(page).toHaveURL(/\/entrar/);
  await pausa(page, 1400);
  await foto(page, 'entrar');
  await page.getByRole('button', { name: 'Preencher' }).click();
  await pausa(page);
  await page.getByRole('button', { name: 'Entrar', exact: true }).click();
  await expect(page.getByRole('heading', { name: 'Painel', level: 1 })).toBeVisible();

  // 2. Emite e paga a nota de um cliente fictício: o Painel passa a ter valor recebido.
  await page.getByRole('link', { name: 'Notas', exact: true }).click();
  const barbearia = linhaDe(page, 'Barbearia Navalha');
  await expect(barbearia).toContainText('Pendente');
  await pausa(page);
  await barbearia.getByRole('button', { name: 'Emitir' }).click();
  const emitir = page.getByRole('dialog');
  await emitir.locator('input[formcontrolname="numero"]').fill('1042');
  await pausa(page, 1200);
  await emitir.getByRole('button', { name: 'Emitir', exact: true }).click();
  await expect(barbearia.locator('.situacao')).toHaveText('Emitida');
  await pausa(page);
  await barbearia.getByRole('button', { name: 'Pagar' }).click();
  const pagar = page.getByRole('dialog');
  await pausa(page, 1000);
  await pagar.getByRole('button', { name: 'Pagar', exact: true }).click();
  await expect(barbearia.locator('.situacao')).toHaveText('Paga');
  await foto(page, 'notas');

  // 3. As outras páginas, já com o dinheiro entrando.
  await page.getByRole('link', { name: 'Painel', exact: true }).click();
  await expect(page.locator('article.card.recebido')).toContainText(/R\$\s300,00/);
  await foto(page, 'painel', true);

  await page.getByRole('link', { name: 'Financeiro' }).click();
  await expect(page.getByText('Demonstrativo do período')).toBeVisible();
  await foto(page, 'financeiro', true);

  await page.getByRole('link', { name: 'Clientes' }).click();
  await expect(page.locator('tr.mat-mdc-row')).toHaveCount(3);
  await foto(page, 'clientes');

  await page.getByRole('link', { name: 'Despesas' }).click();
  await expect(page.locator('tr.mat-mdc-row').first()).toBeVisible();
  await foto(page, 'despesas');

  // 4. Fecha a página para o Playwright terminar de gravar o vídeo e o salva na pasta de saída.
  const video = page.video();
  await page.close();
  await video?.saveAs(`${SAIDA}/demo.webm`);
});
