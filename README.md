# FinTrack 360

> **PROTOTIPO EDUCATIVO — DATOS COMPLETAMENTE SIMULADOS**
>
> No se conecta con bancos, listas, juzgados, centrales de riesgo ni entidades públicas. Las personas,
> documentos, cuentas, procesos y resultados son ficticios. Los nombres de listas y entidades aparecen solo
> como etiquetas de fuentes simuladas: ninguna fuente real se consulta.

Plataforma ficticia de perfil 360°: consolidación bancaria, historial crediticio, debida diligencia, listas,
PEP, antecedentes, información judicial y de tránsito, alertas, reporte y un asistente (FinTrack AI).

## Cómo ejecutarlo

### Con Docker (recomendado)

Requiere Docker Desktop.

```powershell
.\scripts\iniciar.ps1
```

El script crea `docker/.env` con secretos aleatorios la primera vez, levanta PostgreSQL, backend y frontend, y
muestra la contraseña de demostración. Luego abre <http://localhost:5173> e ingresa con el usuario `analista`.

Sin el script: copia `docker/.env.example` a `docker/.env`, completa los valores y ejecuta
`docker compose -f docker/docker-compose.yml up --build`.

### Sin Docker (demo local)

Requiere JDK 21+, Maven y Node 20+.

```powershell
.\scripts\demo-local.ps1
```

El backend arranca con un PostgreSQL embebido y temporal (los datos se reinician en cada arranque) y escribe en
su consola la contraseña temporal del usuario `analista`.

## Pruebas

```bash
cd backend && mvn test          # reglas del proyecto + integración (Flyway, semilla y API)
cd frontend && npm run build    # verificación de tipos y compilación
```

## Qué incluye

| Sección | Contenido |
|---|---|
| Dashboard | Perfil, tarjetas (bancos, cuentas, deuda, créditos, procesos, hallazgos, score) y actividad reciente |
| Perfil 360° | Resumen de todo el perfil y comentarios (crear, editar, eliminar) |
| Bancos / Cuentas / Créditos | 7 bancos ficticios, cuentas, movimientos, tarjetas, obligaciones, pagos y mora, con filtros |
| Historial crediticio | Obligaciones, cuotas, reestructuraciones, cierres y score simulado con sus factores |
| Debida diligencia | 69 fuentes simuladas con su estado y la sección "Fuentes no disponibles" |
| Listas / PEP / Antecedentes | Listas vinculantes y restrictivas, 7 subcategorías PEP y 7 fuentes de antecedentes |
| Judicial / Tránsito | Procesos por documento y por nombre, demandas, medidas, cobranza jurídica, 19 ciudades; comparendos, multas y acuerdos |
| Alertas / Reporte | Hallazgos por nivel y reporte visual de 18 secciones (imprimible) |
| Buscador y FinTrack AI | Búsqueda global en la base ficticia y asistente que solo lee el perfil ya procesado |

Datos semilla: 10 personas, 7 bancos, 30 cuentas, 200 movimientos, 20 créditos, 200 pagos, 15 procesos,
10 demandas, 5 medidas cautelares y 30 alertas. Son deterministas (semilla fija y fecha base 03/10/2026).

## Reglas que el código hace cumplir

- **Un fallo nunca es "sin hallazgos".** Cada fuente devuelve Disponible, Sin hallazgos, Hallazgo, No registra,
  Error en fuente o No disponible. Con fuentes sin respuesta el estado general pasa a "verificación parcial".
- **Nombre ≠ documento.** Una coincidencia solo por nombre se muestra como "coincidencia encontrada" con el aviso
  de homónimos; solo la coincidencia por documento es "confirmada".
- **Cuentas enmascaradas.** La API nunca devuelve un número completo, solo `****1234`.
- **Dinero exacto.** `NUMERIC(18,2)`; la base de datos rechaza un movimiento cuyo saldo posterior no sea el
  saldo anterior ± el valor.
- **Secretos por variables de entorno.** `JWT_SECRET`, `DB_PASSWORD` y `APP_DEMO_PASSWORD`; sin ellas el backend
  no arranca.
- **Sin datos reales en texto libre.** Comentarios y preguntas a la IA rechazan secuencias que parezcan
  documentos o cuentas y palabras como contraseña o token.

## FinTrack AI

Es un motor local de reglas, no un modelo de lenguaje: no llama a ningún servicio externo, solo lee el perfil
ficticio que el backend ya procesó y separa **DATOS DEL SISTEMA** de **INTERPRETACIÓN DE IA**. Si la pregunta no
se puede responder con esos datos, lo dice. El punto para conectar un modelo real es
`backend/src/main/java/com/fintrack/ai/LocalAiEngine.java`.

## Estructura

```
backend/   API Spring Boot 3 (Java 21): config, security, entity, repository, dto, service, controller,
           mock, exception, seed, ai, audit · Flyway en resources/db/migration
frontend/  React + TypeScript + Tailwind + Recharts (Vite): api, context, components, pages, hooks, types, utils
docker/    docker-compose.yml y .env.example
postman/   colección de la API
docs/      ARQUITECTURA.md (arquitectura, modelo de datos, endpoints y flujo)
scripts/   iniciar.ps1 (Docker) y demo-local.ps1 (sin Docker)
```
