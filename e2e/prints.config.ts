import { defineConfig, devices } from '@playwright/test';

/**
 * Gera os prints e o vídeo do README. Não é teste: roda à parte, com `bash scripts/e2e.sh prints`,
 * e só mexe na demonstração (dados fictícios).
 */
export default defineConfig({
  testDir: './prints',
  workers: 1,
  timeout: 120_000,
  reporter: [['list']],
  outputDir: 'resultados-prints',
  use: {
    baseURL: process.env.BASE_URL ?? 'http://localhost:4200',
    locale: 'pt-BR',
    timezoneId: 'America/Sao_Paulo',
    viewport: { width: 1440, height: 900 },
    video: { mode: 'on', size: { width: 1440, height: 900 } },
    // O campo de mês do navegador usa o idioma da interface: sem isto aparece "October 2026".
    launchOptions: { args: ['--lang=pt-BR'] },
  },
  projects: [{ name: 'chromium', use: { ...devices['Desktop Chrome'], viewport: { width: 1440, height: 900 } } }],
});
