import { expect, test } from '@playwright/test';

import { LOGIN_DEMONSTRACAO, entrarComDemonstracao, linhaDe } from './ajudas';

test.describe('SirTech Manager no navegador', () => {
  test('senha errada mostra a mensagem e não entra', async ({ page }) => {
    await page.goto('/entrar');

    await page.locator('input[formcontrolname="email"]').fill(LOGIN_DEMONSTRACAO.email);
    await page.locator('input[formcontrolname="senha"]').fill('senha-errada-123');
    await page.getByRole('button', { name: 'Entrar', exact: true }).click();

    await expect(page.getByRole('alert')).toContainText('E-mail ou senha incorretos');
    await expect(page).toHaveURL(/\/entrar/);
  });

  test('o login de demonstração abre o painel com o previsto do mês', async ({ page }) => {
    await entrarComDemonstracao(page);

    // Os três clientes fictícios somam 300 + 200 + 180, em qualquer mês.
    await expect(page.locator('article.card.previsto')).toContainText(/R\$\s680,00/);
    await expect(page.locator('article.card.despesas')).toContainText(/R\$\s220,00/);
    await expect(page.getByRole('heading', { name: 'Últimos 6 meses' })).toBeVisible();
  });

  test('emitir uma nota pendente a deixa como Emitida e libera o Pagar', async ({ page }) => {
    await entrarComDemonstracao(page);
    await page.getByRole('link', { name: 'Notas', exact: true }).click();

    const barbearia = linhaDe(page, 'Barbearia Navalha');
    await expect(barbearia).toContainText('Pendente');
    await barbearia.getByRole('button', { name: 'Emitir' }).click();

    const caixa = page.getByRole('dialog');
    await caixa.locator('input[formcontrolname="numero"]').fill('E2E-1');
    await caixa.getByRole('button', { name: 'Emitir', exact: true }).click();

    await expect(barbearia).toContainText('Emitida');
    await expect(barbearia).toContainText('E2E-1');
    await expect(barbearia.getByRole('button', { name: 'Pagar' })).toBeVisible();
    // As outras notas continuam pendentes: emitir uma não mexe nas demais.
    await expect(linhaDe(page, 'Pet Shop Amigo Fiel')).toContainText('Pendente');
  });

  test('o Financeiro mostra o demonstrativo dos 6 meses e o total', async ({ page }) => {
    await entrarComDemonstracao(page);
    await page.getByRole('link', { name: 'Financeiro' }).click();

    await expect(page.getByRole('heading', { name: 'Financeiro', level: 1 })).toBeVisible();
    await expect(page.getByText('Demonstrativo do período')).toBeVisible();
    await expect(page.locator('table.meses tbody tr')).toHaveCount(6);
    await expect(page.locator('table.meses tfoot')).toContainText('Total');
    // O Financeiro não repete os cartões do Painel.
    await expect(page.locator('article.card')).toHaveCount(0);
  });

  test('sair encerra a sessão e as páginas voltam a pedir login', async ({ page }) => {
    await entrarComDemonstracao(page);

    await page.getByRole('button', { name: 'Sair' }).click();
    await expect(page).toHaveURL(/\/entrar/);

    await page.goto('/clientes');
    await expect(page).toHaveURL(/\/entrar\?voltar=%2Fclientes/);
  });
});
