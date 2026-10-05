---
name: springboot-tdd
description: Flujo TDD para el backend FinTrack 360 con JUnit 5, Spring Boot Test, MockMvc y PostgreSQL embebido. Úsala al implementar o corregir lógica de negocio, endpoints o reglas de dominio.
---

# TDD en el backend

## Comando
`cd backend && mvn test` — usa PostgreSQL embebido (zonky/otj `embedded-postgres`), no requiere Docker. Prueba una clase: `mvn test -Dtest=RulesTest`.

## Ciclo
1. **Rojo**: escribe primero la prueba que describe el comportamiento (y que falle por la razón correcta).
2. **Verde**: implementación mínima.
3. **Refactor**: limpia con las pruebas en verde.
Corre `mvn test` completo antes de dar algo por terminado.

## Qué probar y dónde
- `RulesTest` (unitarias, sin Spring): reglas puras — `BalanceCalculator` (saldo posterior = saldo anterior ± valor, con `BigDecimal`), `Masking.mask`, `InputGuard.check`, coincidencia por nombre vs. documento.
- `FinTrackIntegrationTest` (`@SpringBootTest` + MockMvc + BD embebida con Flyway): endpoints, seguridad, flujo completo.

## Casos obligatorios del dominio
- Saldos: secuencias de débito/crédito → saldo final exacto, escala 2, sin errores de redondeo.
- Fuente no disponible ⇒ resultado "No disponible", **nunca** "sin hallazgos".
- Coincidencia solo por nombre ⇒ no confirmada; con documento ⇒ confirmada.
- Cuentas enmascaradas en toda respuesta (`****` + 4 dígitos); nunca el número completo.
- Seguridad: 401 sin token/token inválido; 400 si texto libre contiene documento, cuenta, contraseña o token.
- Datos semilla deterministas: dos arranques producen los mismos datos.

## Buenas prácticas
- Nombres que describen el comportamiento (`debeRechazarTextoConDocumento`). Patrón Arrange-Act-Assert.
- Pruebas independientes y sin orden; no dependas de `now()`; inyecta `Clock` si el tiempo importa.
- Sin secretos reales ni personas/documentos reales en fixtures: todo ficticio.
- No uses `@Disabled` ni borres pruebas para ponerlas en verde.
- Mocks solo en los límites (fuentes simuladas); la BD se prueba real (embebida).
