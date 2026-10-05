---
name: docker-patterns
description: Patrones Docker/Docker Compose de FinTrack 360 (PostgreSQL, backend Spring Boot, frontend Nginx, secretos por entorno). Úsala al editar Dockerfiles, `docker/docker-compose.yml` o scripts de arranque.
---

# Docker / Compose

## Servicios (`docker/docker-compose.yml`, proyecto `fintrack360`)
- `db`: `postgres:16-alpine`, volumen `db-data`, healthcheck con `pg_isready`.
- `backend`: build `../backend`, puerto 8080, `depends_on: db: condition: service_healthy`.
- `frontend`: build `../frontend` (Vite → Nginx), publica 5173→80.

## Reglas
- **Secretos solo por entorno**: `${DB_PASSWORD:?mensaje}` hace fallar el arranque si falta. Nunca valores reales en Compose, Dockerfile ni imagen. `docker/.env` fuera de git; solo `.env.example`.
- Variables del backend: `DB_URL, DB_USER, DB_PASSWORD, JWT_SECRET, APP_DEMO_USER, APP_DEMO_PASSWORD, CORS_ALLOWED_ORIGINS, TZ`.
- Dockerfiles **multi-stage**: build (JDK 21 + Maven / Node 22) y runtime mínimo (JRE 21, nginx-alpine). Ejecutar como usuario no root cuando sea posible.
- Capas cacheables: copia `pom.xml`/`package*.json` primero, resuelve dependencias, luego el código. Mantén `.dockerignore` (`node_modules`, `target`, `.env`, `dist`).
- Fija versiones de imagen (`postgres:16-alpine`, no `latest`).
- Healthchecks en `db` (y opcional en backend vía actuator si se añade); `depends_on` con `service_healthy`.
- Solo publica los puertos necesarios; la BD no se expone al host salvo para depuración explícita.
- Imágenes y contenedores no deben contener datos reales; los datos semilla son ficticios y deterministas (Flyway/seeders).
- CORS: `CORS_ALLOWED_ORIGINS` debe coincidir con el origen del frontend (`http://localhost:5173`).

## Comandos
- Todo con Docker: `scripts/iniciar.ps1` (crea `docker/.env` si falta).
- Manual: `cd docker && docker compose up --build -d`; logs `docker compose logs -f backend`; bajar `docker compose down` (con `-v` borra datos).
- Sin Docker: `scripts/demo-local.ps1` (BD embebida).

## Validar
`docker compose config` (sintaxis y variables), luego `up --build` y comprobar `http://localhost:5173` y login.
