---
name: springboot-security
description: Seguridad Spring Security + JWT de FinTrack 360 (autenticación, CORS, secretos, enmascarado, validación de entrada, auditoría). Úsala al tocar login, filtros, rutas protegidas o datos sensibles.
---

# Seguridad FinTrack 360

## Estado actual
- `SecurityConfig`: API stateless, CSRF deshabilitado (no hay cookies de sesión), `/api/auth/login` público, todo lo demás `authenticated()`. 401 devuelve JSON `NO_AUTENTICADO`.
- `JwtAuthFilter` + `JwtService` (jjwt) validan `Authorization: Bearer`. Contraseñas con BCrypt.
- CORS solo para `app.cors.allowed-origins` (env `CORS_ALLOWED_ORIGINS`), métodos y headers mínimos.

## Reglas
- **Secretos solo por variables de entorno** (`JWT_SECRET`, `DB_PASSWORD`, `APP_DEMO_PASSWORD`). Nunca en código, `application.yml`, tests, Postman ni commits. `docker/.env` no se versiona; solo `.env.example`.
- `JWT_SECRET` ≥ 256 bits; el arranque debe fallar si falta (sin valor por defecto).
- Toda ruta nueva queda protegida por defecto; abrir una exige justificarlo. Si añades roles: `@PreAuthorize` + `@EnableMethodSecurity`, y prueba 401 y 403.
- Nunca confiar en IDs del cliente para autorización sin verificar pertenencia/rol.
- Errores de login genéricos (no revelar si existe el usuario). Sin enumeración de usuarios.
- Entrada de texto libre: `InputGuard.check` rechaza documentos, números largos, contraseñas y tokens. Validación Bean Validation (`@Valid`, `@Size`, `@NotBlank`) en todos los request DTO.
- Cuentas siempre enmascaradas (`Masking.mask` → `****4582`); no exponer el número completo en DTOs, logs ni reportes.
- Consultas con parámetros enlazados; jamás concatenar SQL/JPQL con entrada del usuario.
- No loguear tokens, contraseñas, documentos ni cuerpos de request.
- Auditoría: login, generación de reportes y consultas de debida diligencia pasan por `AuditService`.
- Reglas de dominio: nunca conectar con bancos, listas, juzgados, centrales o APIs gubernamentales reales; todo es ficticio.

## Pruebas obligatorias
Sin token → 401; token inválido/expirado → 401; texto con documento/cuenta → 400; respuesta sin número de cuenta completo. Usa `spring-security-test`.
