import { defineConfig, devices } from '@playwright/test';

/**
 * Teste de ponta a ponta: navegador de verdade contra o sistema inteiro (tela, API e banco).
 * Quem sobe o sistema é o scripts/e2e.sh, que também define BASE_URL.
 */
export default defineConfig({
  testDir: './tests',
  // Os cenários compartilham o mesmo banco de demonstração; rodar um de cada vez evita surpresa.
  workers: 1,
  fullyParallel: false,
  retries: process.env.CI ? 1 : 0,
  timeout: 30_000,
  expect: { timeout: 8_000 },
  reporter: [['list'], ['html', { outputFolder: 'relatorio', open: 'never' }]],
  outputDir: 'resultados',
  use: {
    baseURL: process.env.BASE_URL ?? 'http://localhost:4200',
    locale: 'pt-BR',
    timezoneId: 'America/Sao_Paulo',
    // Só guarda prova quando algo falha: o vídeo e o trace explicam o que o navegador viu.
    trace: 'retain-on-failure',
    video: 'retain-on-failure',
    screenshot: 'only-on-failure',
  },
  projects: [
    {
      name: 'chromium',
      use: { ...devices['Desktop Chrome'], viewport: { width: 1440, height: 900 } },
    },
  ],
});
