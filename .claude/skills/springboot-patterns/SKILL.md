---
name: springboot-patterns
description: Patrones de arquitectura Spring Boot 3 / Java 21 del backend FinTrack 360 (capas, DTOs, servicios, excepciones, configuración). Úsala al añadir endpoints, servicios o DTOs en `backend/`.
---

# Backend FinTrack 360 (Spring Boot 3, Java 21)

Paquete base `com.fintrack`: `config, security, entity, repository, dto, service, controller, mock, exception, seed, ai, audit`.

## Capas
- **controller**: solo HTTP (validación `@Valid`, mapeo de ruta, códigos de estado). Sin lógica ni acceso a repositorios.
- **service**: lógica de negocio y transacciones. `@Transactional(readOnly = true)` a nivel de clase en lecturas; `@Transactional` solo en métodos que escriben (ver `ProfileService`, `ReportService`).
- **repository**: interfaces Spring Data. Consultas derivadas o `@Query` con parámetros nombrados.
- **dto**: `record`s inmutables. Nunca devolver entidades JPA desde un controlador.
- **mock**: `SourceSimulator`/`MockApiController` simulan fuentes externas. Es lo único que "consulta fuentes", y todo es ficticio.

## Reglas
- Inyección por constructor (sin `@Autowired` en campos).
- Excepciones de negocio: `BadRequestException`, `NotFoundException`, `UnauthorizedException`; las traduce `GlobalExceptionHandler` a JSON `{error, message}` en español. No captures y silencies.
- Si una fuente simulada falla, el resultado es estado `NO_DISPONIBLE`/error, **nunca** una lista vacía presentada como "sin hallazgos".
- Coincidencia por nombre ≠ confirmada: `MatchType` distingue; confirmar exige coincidir el documento.
- Dinero: `BigDecimal` (escala 2), nunca `double`. Saldo posterior = saldo anterior ± valor, centralizado en `BalanceCalculator`.
- Texto libre del usuario pasa por `InputGuard.check`.
- Números de cuenta salen por `Masking.mask`.
- Acciones sensibles se registran con `AuditService`.
- Configuración en `application.yml` con `${VAR}`; secretos solo por variables de entorno (`DB_PASSWORD`, `JWT_SECRET`, `APP_DEMO_PASSWORD`).
- Mensajes de error y logs de usuario en español; sin datos personales en logs.

## Checklist para un endpoint nuevo
DTO record → método de servicio (transacción correcta) → controlador fino → prueba en `FinTrackIntegrationTest` (o clase nueva) → `cd backend && mvn test`.
