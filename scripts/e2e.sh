#!/usr/bin/env bash
# Sobe uma cópia descartável do sistema, roda o Playwright e derruba tudo.
#
#   bash scripts/e2e.sh          # testes de ponta a ponta (5 cenários)
#   bash scripts/e2e.sh prints   # refaz os prints e o GIF do README em docs/screenshots
#
# - Usa outro nome de projeto (sirtech-e2e), outros volumes e outras portas: a demonstração que
#   estiver aberta em localhost:4200 não é tocada.
# - O navegador roda num container da Microsoft, ligado à rede do Compose, então nada precisa
#   estar instalado no computador além do Docker. A versão da imagem precisa ser a mesma do
#   @playwright/test em e2e/package.json.
# - Só a demonstração é usada: os dados são fictícios e o volume é apagado no fim (down -v).
set -euo pipefail

MODO="${1:-testes}"
if [ "$MODO" != "testes" ] && [ "$MODO" != "prints" ]; then
  echo "Uso: bash scripts/e2e.sh [testes|prints]" >&2
  exit 2
fi

PROJETO="sirtech-e2e"
IMAGEM="mcr.microsoft.com/playwright:v1.64.1-noble"
IMAGEM_FFMPEG="mwader/static-ffmpeg:7.1.1"
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

if [ "$MODO" = "testes" ]; then
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
  exit 0
fi

echo "==> Tirando os prints e gravando o vídeo"
SAIDA="$RAIZ/docs/screenshots"
mkdir -p "$SAIDA"
docker run --rm --ipc=host \
  --network "${PROJETO}_default" \
  -e BASE_URL="http://web" -e SAIDA="/saida" \
  -e LANGUAGE="pt_BR:pt" -e LC_ALL="pt_BR.UTF-8" -e LANG="pt_BR.UTF-8" \
  -v "$RAIZ/e2e:/e2e" \
  -v "$SAIDA:/saida" \
  -v sirtech-e2e-node-modules:/e2e/node_modules \
  -w /e2e \
  "$IMAGEM" \
  sh -c "npm ci --no-fund --no-audit && npx playwright test -c prints.config.ts"

echo "==> Convertendo o vídeo em GIF (8 quadros por segundo, 960 px de largura)"
# A paleta é calculada a partir do próprio vídeo: o GIF fica nítido com poucas cores.
docker run --rm -v "$SAIDA:/saida" "$IMAGEM_FFMPEG" -y -loglevel error -i /saida/demo.webm \
  -vf "fps=8,scale=960:-1:flags=lanczos,split[a][b];[a]palettegen=max_colors=96:stats_mode=diff[p];[b][p]paletteuse=dither=bayer:bayer_scale=5:diff_mode=rectangle" \
  -loop 0 /saida/demo.gif
rm -f "$SAIDA/demo.webm"
ls -la "$SAIDA"
