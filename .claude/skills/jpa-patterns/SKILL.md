---
name: jpa-patterns
description: Patrones JPA/Hibernate de FinTrack 360 (entidades, relaciones, N+1, transacciones, proyecciones). Úsala al crear o modificar entidades y repositorios.
---

# JPA / Hibernate

## Entidades (`com.fintrack.entity`)
- Heredan de `BaseEntity` cuando aplique. `@Id @GeneratedValue(strategy = IDENTITY)` (esquema `bigserial`).
- Enums con `@Enumerated(EnumType.STRING)`; nunca `ORDINAL`.
- Dinero: `BigDecimal` con `@Column(precision = 18, scale = 2)`.
- Relaciones `@ManyToOne(fetch = LAZY)`; `@OneToMany` solo si se necesita, con `mappedBy`. Sin `EAGER`.
- `equals/hashCode` basados en id (o clave de negocio) — no en todos los campos; sin Lombok `@Data` en entidades.
- Las entidades no salen del servicio: se mapean a `record` DTO.

## N+1 y rendimiento
- Detecta N+1 al listar con relaciones: usa `@EntityGraph` o `join fetch` en la consulta del repositorio.
- No hagas `join fetch` de dos colecciones a la vez (producto cartesiano); trae una y la otra en otra consulta.
- Para listados/tablas usa proyecciones (interface o `record` en `@Query "select new …"`) en vez de entidades completas.
- Paginación con `Pageable`; con `join fetch` de colecciones usa dos pasos (ids y luego fetch).

## Transacciones
- Límite en el **servicio**, no en el controlador. Lecturas: `@Transactional(readOnly = true)`.
- Sin acceso a asociaciones lazy fuera de la transacción: trae lo necesario dentro del servicio y devuelve DTO.
- `spring.jpa.open-in-view: false`.

## Repositorios
- Consultas derivadas simples; `@Query` con `:parametro` nombrado para lo demás. Nunca concatenar cadenas.
- Escrituras masivas del seeder en `@Transactional` con lotes (`saveAll`), datos deterministas.

## Cálculo de saldos
Saldo posterior = saldo anterior ± valor, siempre vía `BalanceCalculator` con `BigDecimal` y `RoundingMode` explícito. Prueba cada cambio (ver `springboot-tdd`).
