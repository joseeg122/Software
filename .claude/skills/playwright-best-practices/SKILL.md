---
name: playwright-best-practices
description: Buenas prácticas de pruebas E2E con Playwright para la app React de FinTrack 360 (selectores, login, esperas, reglas de dominio visibles). Úsala al crear o depurar pruebas end-to-end.
---

# Playwright E2E

## Preparación
Playwright y Chromium vienen preinstalados en el entorno cloud (`PLAYWRIGHT_BROWSERS_PATH=/opt/pw-browsers`); no ejecutes `playwright install`. Si falta el paquete: `npm i -D @playwright/test` en `frontend/` (sin descargar navegadores, `PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD=1`) y usa `executablePath: '/opt/pw-browsers/chromium'` si hace falta.

Levantar la app: backend `cd backend && mvn spring-boot:test-run` (BD embebida; la contraseña temporal de `analista` sale en el log) y frontend `cd frontend && npm run dev` (:5173). Configura `webServer` en `playwright.config.ts` y pasa credenciales por variables de entorno (`E2E_USER`, `E2E_PASSWORD`), nunca en el código.

## Prácticas
- Selectores por rol/texto accesible (`getByRole`, `getByLabel`) o `data-testid` estable; evita CSS frágil y XPath.
- **Sin `waitForTimeout`**: usa auto-wait y aserciones web-first (`await expect(locator).toBeVisible()`).
- Login una vez con `storageState` en un `globalSetup`/proyecto "setup"; las pruebas reutilizan la sesión.
- Pruebas independientes (sin orden), una intención cada una; nombres en español descriptivos.
- Datos semilla deterministas: asserts sobre valores conocidos del seeder, no sobre fechas "ahora".
- Trazas en fallo: `trace: 'on-first-retry'`, `screenshot: 'only-on-failure'`.
- Page Objects ligeros solo cuando se repite lógica (Login, Layout/sidebar).

## Qué cubrir (reglas del producto)
1. Aviso "PROTOTIPO EDUCATIVO — DATOS COMPLETAMENTE SIMULADOS" visible en login y en páginas internas.
2. Login correcto/incorrecto; ruta protegida redirige a login sin sesión.
3. Cuentas siempre enmascaradas (`/\*{4}\d{4}/`); nunca un número completo en el DOM.
4. Fuente no disponible muestra "No disponible", no "Sin hallazgos".
5. Coincidencia por nombre se etiqueta como posible, no confirmada.
6. Texto libre con documento/cuenta/contraseña muestra el rechazo del backend.
7. Máximo 2–3 clics: del sidebar al dato (Perfil 360, Créditos, Judicial, PEP, Reporte…).

## Comandos
`npx playwright test` · `--ui` para depurar · `npx playwright show-report`.
