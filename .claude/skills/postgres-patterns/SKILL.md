---
name: postgres-patterns
description: Patrones PostgreSQL + Flyway de FinTrack 360 (migraciones, tipos, índices, dinero NUMERIC(18,2), datos semilla deterministas). Úsala al cambiar el esquema o escribir consultas.
---

# PostgreSQL + Flyway

## Migraciones
- Ubicación: `backend/src/main/resources/db/migration/`. Esquema actual en `V1__schema.sql`.
- **Nunca edites una migración ya aplicada**; crea `V2__descripcion.sql`, `V3__…`. Un cambio lógico por archivo, nombres en snake_case, comentario inicial con propósito.
- `spring.jpa.hibernate.ddl-auto` en `validate` (o `none`); el esquema lo manda Flyway, no Hibernate.
- Migraciones idempotentes en intención: constraints con nombre, `create index` explícito.

## Tipos y convenciones
- Dinero: `NUMERIC(18,2)` (entidad `BigDecimal`). Nunca `float`/`double`/`money`.
- Fechas: `date` para fechas de negocio, `timestamp` para eventos (como el esquema actual).
- Claves: `bigserial primary key`. Foreign keys siempre declaradas con `references`.
- Estados y severidades como `varchar` + enum Java (`@Enumerated(STRING)`), con `check` cuando sea estable.
- Documentos y cuentas son ficticios. Si guardas cuentas, enmascaradas (`****4582`) o solo últimos 4 dígitos.

## Índices y consultas
- Indexa FKs consultadas (`person_id`, `account_id`) y columnas de filtro/orden (`transactions(account_id, occurred_at desc)`).
- Revisa planes con `EXPLAIN (ANALYZE, BUFFERS)` antes de añadir índices; no indexes "por si acaso".
- Paginación por keyset o `LIMIT/OFFSET` con orden estable; nunca `select *` hacia DTOs.
- Búsqueda de texto: normaliza (minúsculas/sin tildes) de forma consistente con `SearchService`; coincidencia por nombre ≠ confirmada.

## Datos semilla
Deterministas: `DataSeeder`/`TransactionGenerator` usan semilla fija (`Random(seed)`), sin `now()` ni UUID aleatorios en los datos de negocio, para que las pruebas sean reproducibles. Saldo posterior = saldo anterior ± valor.

## Probar
`cd backend && mvn test` usa PostgreSQL embebido (sin Docker). Toda migración nueva debe aplicar limpia sobre BD vacía.
