#!/usr/bin/env bash
# Teste de ponta a ponta: sobe uma cópia descartável do sistema, roda o Playwright e derruba tudo.
#
#   bash scripts/e2e.sh
#
# - Usa outro nome de projeto (sirtech-e2e), outros volumes e outras portas: a demonstração que
#   estiver aberta em localhost:4200 não é tocada.
# - O navegador roda num container da Microsoft, ligado à rede do Compose, então o teste não
#   depende de nada instalado no computador além do Docker. A versão da imagem precisa ser a
#   mesma do @playwright/test em e2e/package.json.
# - Só a demonstração é usada: os dados são fictícios e o volume é apagado no fim (down -v).
set -euo pipefail

PROJETO="sirtech-e2e"
IMAGEM="mcr.microsoft.com/playwright:v1.64.1-noble"
RAIZ="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
# No Git Bash do Windows, o Docker precisa do caminho no estilo C:/...
if command -v cygpath >/dev/null 2>&1; then
  RAIZ="$(cygpath -m "$RAIZ")"
  export MSYS_NO_PATHCONV=1
fi

export WEB_PORT="${E2E_WEB_PORT:-14200}" API_PORT="${E2E_API_PORT:-18080}" POSTGRES_PORT="${E2E_DB_PORT:-15432}"

derrubar() {
  echo "==> Derrubando o ambiente de teste"
  docker compose -p "$PROJETO" down -v --remove-orphans >/dev/null 2>&1 || true
}
trap derrubar EXIT

echo "==> Subindo o sistema (projeto $PROJETO)"
docker compose -p "$PROJETO" up -d --build

echo "==> Esperando a API responder"
for i in $(seq 1 90); do
  if curl -fs "http://localhost:${API_PORT}/api/acesso/demonstracao" >/dev/null 2>&1; then
    break
  fi
  if [ "$i" -eq 90 ]; then
    echo "A API não respondeu a tempo. Últimas linhas do log:" >&2
    docker compose -p "$PROJETO" logs --tail 40 api >&2
    exit 1
  fi
  sleep 2
done

echo "==> Rodando o Playwright"
mkdir -p "$RAIZ/e2e/resultados" "$RAIZ/e2e/relatorio"
docker run --rm --ipc=host \
  --network "${PROJETO}_default" \
  -e BASE_URL="http://web" -e CI="${CI:-}" \
  -v "$RAIZ/e2e:/e2e" \
  -v sirtech-e2e-node-modules:/e2e/node_modules \
  -w /e2e \
  "$IMAGEM" \
  sh -c "npm ci --no-fund --no-audit && npx playwright test"
