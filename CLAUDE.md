# FinTrack 360 — Reglas del proyecto (Claude Code)

PROTOTIPO EDUCATIVO. **Todo es ficticio.** El prompt completo está en `PROMPT.md`.

## Reglas no negociables
- Nunca conectar con bancos, listas, juzgados, centrales de riesgo ni APIs gubernamentales reales.
- Nunca usar personas, documentos o cuentas reales. Nunca afirmar que una fuente real fue consultada.
- Un error o fuente no disponible nunca se muestra como "sin hallazgos".
- Coincidencia por nombre ≠ coincidencia confirmada (documento).
- Números de cuenta siempre enmascarados (`****4582`).
- Aviso visible en toda la app: "PROTOTIPO EDUCATIVO — DATOS COMPLETAMENTE SIMULADOS".
- Secretos solo por variables de entorno; nunca en el código.

## Stack
Frontend: React + TypeScript + Tailwind + Recharts (Vite). Backend: Java 21 + Spring Boot 3 + Spring Security + JWT + JPA + Flyway. BD: PostgreSQL. Infra: Docker Compose.

## Estructura
- `backend/` API Spring Boot (paquete `com.fintrack`: config, security, entity, repository, dto, service, controller, mock, exception, seed, ai, audit)
- `frontend/` app React (api, context, components, pages, hooks, types, utils)
- `docker/`, `postman/`, `docs/`, `scripts/`

## Convenciones
- Interfaz sencilla: tarjetas, tablas, sidebar; máximo 2–3 clics a cualquier dato.
- Dinero en `NUMERIC(18,2)`; saldo posterior = saldo anterior ± valor (con tests).
- Datos semilla deterministas vía Flyway/seeders.
- Responde y documenta en español.
- Trabaja por fases (ver `docs/ARQUITECTURA.md` sección 6) y prueba cada fase antes de seguir.

## Comandos
- Pruebas backend: `cd backend && mvn test` (usa PostgreSQL embebido; no requiere Docker).
- Compilar frontend: `cd frontend && npm run build`.
- Todo con Docker: `scripts/iniciar.ps1`. Sin Docker: `scripts/demo-local.ps1`.
