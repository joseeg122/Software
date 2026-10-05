# FinTrack 360 — frontend (prototipo educativo, datos simulados)
FROM node:22-alpine AS build
WORKDIR /app
COPY package.json package-lock.json ./
RUN npm ci --no-audit --no-fund
COPY . .
RUN npm run build

FROM nginx:1.27-alpine
# nginx sustituye ${PORT} , ${BACKEND_URL} y ${NGINX_LOCAL_RESOLVERS} al arrancar (plantillas de la imagen oficial).
ENV PORT=80 BACKEND_URL=http://backend:8080
COPY nginx.conf.template /etc/nginx/templates/default.conf.template
COPY --from=build /app/dist /usr/share/nginx/html
EXPOSE 80
