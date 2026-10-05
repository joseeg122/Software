---
name: frontend-patterns
description: Patrones de React + TypeScript + Tailwind + Recharts para el frontend de FinTrack 360. Úsala al crear o modificar páginas, componentes, hooks o llamadas a la API en `frontend/`.
---

# Frontend FinTrack 360 (React 18 + TS + Vite + Tailwind + Recharts)

## Estructura (`frontend/src`)
`api/client.ts` (único punto de acceso HTTP) · `context/` (Auth, Profile) · `components/` (ui.tsx, status.tsx, Layout) · `pages/` (una por módulo) · `hooks/` · `types/index.ts` · `utils/format.ts`.

## Reglas
- Toda llamada HTTP pasa por `api/client.ts`; nunca `fetch` suelto en componentes. El token JWT se adjunta allí; un 401 cierra sesión.
- Tipos de la API en `types/index.ts`. Sin `any`; `npm run build` ejecuta `tsc --noEmit` y debe pasar.
- Estado compartido del perfil vía `ProfileContext`/`useProfile`; no dupliques fetch del mismo perfil en cada página.
- Formato de dinero/fechas solo con `utils/format.ts` (es-CO, COP). Nunca formatear con `toFixed` a mano.
- Cada página maneja 4 estados: cargando, error, vacío, datos.
- Máximo 2–3 clics a cualquier dato: tarjetas + tablas + sidebar, sin navegación profunda.

## Reglas de dominio que la UI debe respetar
- Aviso visible en toda la app: "PROTOTIPO EDUCATIVO — DATOS COMPLETAMENTE SIMULADOS" (en `Layout`; no lo quites ni lo ocultes).
- Una fuente con error/no disponible se muestra como **"No disponible"** (estado propio en `status.tsx`), jamás como "Sin hallazgos".
- Coincidencia por nombre se etiqueta "Posible coincidencia (por nombre)"; solo "Confirmada" si coincide el documento.
- Números de cuenta se muestran como llegan enmascarados (`****4582`); nunca reconstruirlos ni pedirlos en formularios.
- Los textos libres (comentarios, IA) no deben aceptar documentos/cuentas/contraseñas; el backend los rechaza, la UI muestra el mensaje.

## Componentes y gráficos
- Reutiliza `ui.tsx` (Card, Table, Badge…) antes de crear componentes nuevos.
- Recharts dentro de `ResponsiveContainer`; colores desde un solo mapa de severidad; siempre con leyenda/tooltip y etiqueta accesible.
- Elementos interactivos con `aria-label`; añade `data-testid` estable solo donde no haya rol/etiqueta accesible (aún no hay ninguno en el código) para las pruebas Playwright.

## Verificación
`cd frontend && npm run build`. Para cambios visibles, levanta `npm run dev` (backend en :8080) y comprueba la página.
