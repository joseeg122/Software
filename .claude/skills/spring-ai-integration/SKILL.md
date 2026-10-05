---
name: spring-ai-integration
description: Guía para integrar Spring AI (o cualquier LLM) en el módulo `ai` de FinTrack 360 sin romper las reglas del prototipo. Úsala si se quiere reemplazar o complementar `LocalAiEngine` por un modelo de lenguaje.
---

# Spring AI en FinTrack 360

## Punto de partida
Hoy `com.fintrack.ai.LocalAiEngine` es un **motor local de reglas**: no llama a servicios externos, solo lee el `ProfileSnapshot` ficticio y, si no puede responder con esos datos, lo dice. Devuelve `AiResponse` con `ENGINE` y `NOTICE`. Esto es el comportamiento por defecto y debe seguir funcionando sin red ni claves.

## Reglas no negociables
- El LLM **solo recibe datos simulados** del `ProfileSnapshot`. Nunca documentos, cuentas, personas ni fuentes reales; las cuentas llegan enmascaradas (`****4582`).
- El modelo no consulta fuentes ni afirma que una fuente real fue consultada. Una fuente no disponible se declara "no disponible", jamás "sin hallazgos".
- Coincidencia por nombre ≠ confirmada: el prompt lo exige y la respuesta debe conservarlo.
- La respuesta mantiene el aviso (`NOTICE`) de que es automática, simulada y no constituye concepto real.
- La pregunta del usuario pasa por `InputGuard.check` antes de llegar al modelo (rechaza documentos, cuentas, contraseñas, tokens).
- **Claves por variable de entorno** (`SPRING_AI_…_API_KEY` u otra); nunca en código, yml ni commits.
- Un LLM externo es **opcional** y requiere decisión explícita del equipo (el prototipo promete no depender de servicios externos). Actívalo por perfil/propiedad (`app.ai.provider=local|llm`), con `local` por defecto.

## Diseño recomendado
1. Extrae una interfaz `AiEngine { AiResponse answer(String question, ProfileSnapshot p); }`; `LocalAiEngine` la implementa y sigue siendo el valor por defecto (`@ConditionalOnProperty(app.ai.provider, havingValue = "local", matchIfMissing = true)`).
2. Crea `LlmAiEngine` con `ChatClient` de Spring AI (`spring-ai-starter-model-*` del proveedor elegido; añade el BOM `spring-ai-bom` en `pom.xml`).
3. Prompt de sistema fijo, en español: responder solo con el contexto dado, citar los datos usados, decir "no hay datos suficientes" cuando falte, no inventar, no dar concepto legal/financiero real.
4. Contexto = serialización mínima del `ProfileSnapshot` (sin campos innecesarios). Límite de tamaño y de tokens; temperatura baja.
5. **Fallback**: ante timeout/error del modelo, devuelve la respuesta de `LocalAiEngine` o un mensaje "servicio de IA no disponible"; nunca una respuesta vacía que parezca "sin hallazgos".
6. Audita cada consulta con `AuditService` (sin guardar el contexto completo ni claves).

## Pruebas
- Unitarias de `LlmAiEngine` con `ChatClient` simulado (mock): verifica prompt, saneado de entrada, fallback y aviso.
- Una prueba que verifique que el contexto enviado no contiene números de cuenta completos.
- Nunca pruebas que dependan de red o de una clave real. `cd backend && mvn test` debe pasar sin variables de IA.
