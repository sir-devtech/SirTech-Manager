# Etapa 1: gera os arquivos estáticos.
FROM node:24-alpine AS build
WORKDIR /fonte
COPY package.json package-lock.json ./
RUN npm ci
COPY . .
RUN npx ng build --output-path dist

# Etapa 2: o nginx serve a tela e encaminha /api para o container da API.
FROM nginx:1.29-alpine
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /fonte/dist/browser /usr/share/nginx/html
EXPOSE 80
