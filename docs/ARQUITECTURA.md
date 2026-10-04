# FinTrack 360 — Arquitectura

> PROTOTIPO EDUCATIVO — DATOS COMPLETAMENTE SIMULADOS. Ninguna fuente real se consulta.

## 1. Arquitectura

```
Navegador
   │
   ▼
Frontend  React + TypeScript + Tailwind + Recharts   (nginx :5173 en Docker, Vite en desarrollo)
   │  /api  (JWT en cabecera Authorization: Bearer)
   ▼
Backend  Spring Boot 3 · Java 21  (:8080)
   controller ──> service ──> repository (JPA) ──> PostgreSQL 16
        │             │
        │             ├── mock/   SourceSimulator: simula las 69 fuentes en proceso, sin red
        │             ├── ai/     LocalAiEngine: motor de reglas que solo lee el perfil
        │             └── audit/  AuditService: bitácora en audit_log
        └── security/  JwtService + JwtAuthFilter (sin sesión de servidor)

Flyway crea el esquema (V1__schema.sql). seed/DataSeeder carga los datos ficticios al primer arranque.
```

Decisiones:

- **Un solo perfil consolidado.** `ProfileService.snapshot` arma el perfil 360° completo. Lo usan la pantalla,
  el reporte (guardado como JSON) y FinTrack AI, de modo que los tres ven siempre lo mismo.
- **Fuentes simuladas en proceso.** `SourceSimulator` no abre ninguna conexión: decide el resultado a partir de
  los registros ficticios de la base de datos y del número de consulta, de forma determinista.
- **Datos semilla en Java.** `DataSeeder` usa `Random(42)` y una fecha base fija (03/10/2026), así que la base
  queda idéntica en cada instalación.

## 2. Modelo de datos

Las 23 tablas del enunciado más `persons` (las personas ficticias consultadas; `users` es solo para el login) y
`audit_log`.

| Tabla | Contenido |
|---|---|
| `users` | Usuarios de la aplicación (contraseña con BCrypt) |
| `persons` | Personas ficticias, estado general, fecha y número de la última consulta |
| `banks` | Los 7 bancos ficticios |
| `accounts`, `transactions` | Cuentas y movimientos con saldo anterior y posterior |
| `credits`, `credit_payments`, `credit_history`, `credit_scores` | Obligaciones, cuotas, eventos y score simulado |
| `sources` | Catálogo de las 69 fuentes simuladas y su categoría |
| `due_diligence_checks` | Cada ejecución de "Actualizar consulta" |
| `source_results` | Estado vigente de cada fuente para cada persona |
| `sanctions`, `pep_records`, `background_checks` | Coincidencias simuladas en listas, PEP y antecedentes |
| `judicial_processes`, `lawsuits`, `legal_measures` | Procesos, demandas y medidas cautelares |
| `traffic_records`, `reputational_news` | Tránsito, noticias e información general |
| `alerts`, `comments`, `reports`, `timeline_events` | Hallazgos, comentarios, reportes y actividad reciente |
| `audit_log` | Bitácora de inicios de sesión, consultas, reportes y comentarios |

## 3. Relaciones

```
persons 1─N accounts 1─N transactions
persons 1─N credits  1─N credit_payments
                     1─N credit_history
persons 1─N credit_scores · alerts · comments · reports · timeline_events · due_diligence_checks
banks   1─N accounts · credits · sources (categoría BANCO)

sources 1─N source_results N─1 persons          (único por persona y fuente)
due_diligence_checks 1─N source_results

sources 1─N sanctions · pep_records · background_checks · judicial_processes · traffic_records · reputational_news
persons 1─N (las mismas tablas)                  todas con match_type: NOMINAL | CONFIRMADA
judicial_processes 1─N lawsuits · legal_measures
```

Restricciones relevantes: dinero en `NUMERIC(18,2)`; `chk_tx_balance` exige
`balance_after = balance_before ± amount`; números de cuenta, crédito, proceso y demanda son únicos.

## 4. Endpoints

Todos requieren JWT excepto el login.

| Grupo | Método y ruta |
|---|---|
| Sesión | `POST /api/auth/login` · `GET /api/auth/me` |
| Personas | `GET /api/persons` · `GET /api/persons/{id}` · `GET /api/persons/{id}/dashboard` · `GET /api/persons/{id}/profile` |
| Consulta | `POST /api/persons/{id}/refresh` (Actualizar consulta) · `GET /api/persons/{id}/timeline` |
| Financiero | `GET /api/persons/{id}/banks` · `/accounts` · `/transactions` · `/credits` · `/credit-payments` · `/credit-history` · `/score` |
| Debida diligencia | `GET /api/persons/{id}/due-diligence[?category=]` · `/sources/unavailable` · `/sanctions` · `/pep` · `/background` · `/traffic` · `/news` · `/alerts` |
| Judicial | `GET /api/persons/{id}/judicial/processes` · `/judicial/lawsuits` · `/judicial/measures` |
| Comentarios | `GET, POST /api/persons/{id}/comments` · `PUT, DELETE /api/comments/{id}` |
| Reportes | `POST, GET /api/persons/{id}/reports` · `GET /api/reports/{id}` |
| Herramientas | `GET /api/search?q=` · `POST /api/persons/{id}/ai/ask` · `GET /api/banks` · `GET /api/sources` |
| APIs simuladas | `GET /api/mock/{banco}` · `/api/mock/{banco}/accounts?document=` · `/api/mock/{banco}/credits?document=` · `GET /api/mock/sources/{code}?document=&run=` |

`{banco}`: `banco-nova`, `banco-andino`, `banco-capital`, `banco-horizonte`, `banco-centralia`, `banco-union`,
`banco-continental`.

Errores: `{ "error": "CÓDIGO", "message": "texto" }` con 400, 401, 404 o 500.

## 5. Flujo de información

1. **Inicio de sesión.** `POST /api/auth/login` valida la contraseña (BCrypt) y entrega un JWT. El frontend lo
   guarda en `sessionStorage` y lo envía en cada petición.
2. **Carga del perfil.** Al elegir una persona ficticia, el frontend pide `/profile` una sola vez y todas las
   pantallas leen de ese perfil (máximo 2 o 3 clics a cualquier dato).
3. **Actualizar consulta.** `POST /refresh` → `DueDiligenceService.run` aumenta el número de consulta y pide a
   `SourceSimulator` el resultado de cada fuente:
   - la fuente falla (≈4 % error, ≈5 % no disponible) → `ERROR` o `NO_DISPONIBLE`;
   - banco con productos → `DISPONIBLE`; sin productos → `NO_REGISTRA`;
   - con registros → `HALLAZGO`, `CONFIRMADA` solo si alguno coincide por documento, si no `NOMINAL`;
   - sin registros → `SIN_HALLAZGOS` o `NO_REGISTRA` según la fuente.

   Se actualizan `source_results`, la fecha de consulta y el estado general, que solo es "VERIFICACIÓN
   COMPLETADA" cuando ninguna fuente falló.
4. **Reporte.** `POST /reports` guarda el perfil como JSON con un ID ficticio (`RPT-SIM-…`), la fecha y el estado
   de consulta. El reporte no cambia aunque después se actualice la consulta.
5. **FinTrack AI.** `POST /ai/ask` entrega al motor local el mismo perfil. El motor no consulta fuentes; responde
   con líneas de datos del sistema y una interpretación aparte.

## 6. Fases de construcción

| Fase | Alcance | Verificación |
|---|---|---|
| 1 | Esquema Flyway, entidades y semilla | Prueba de integración: cantidades y saldos encadenados |
| 2 | Seguridad JWT, API, simulador, reporte, buscador, IA | 14 pruebas (`mvn test`) y prueba manual por HTTP |
| 3 | Frontend: 14 pantallas, buscador, IA, comentarios | Verificación de tipos y compilación (`npm run build`) |
| 4 | Docker Compose, scripts, Postman y documentación | Pendiente de probar en un equipo con Docker |
