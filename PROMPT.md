Crea una aplicación web full-stack llamada FINTRACK 360.

IMPORTANTE:
Mantén exactamente la misma filosofía del proyecto anterior:
- Interfaz sencilla.
- Moderna.
- Fácil de entender.
- Pocas pantallas innecesarias.
- Dashboard central.
- Navegación mediante sidebar.
- Información organizada por tarjetas y tablas.
- No crear una aplicación excesivamente compleja.

TODO EL SISTEMA DEBE UTILIZAR DATOS FICTICIOS.

NO conectarse a bancos reales.
NO conectarse a centrales de riesgo reales.
NO consultar personas reales.
NO consultar procesos judiciales reales.
NO consultar bases gubernamentales reales.
NO utilizar credenciales reales.
NO utilizar datos personales reales.

El objetivo es crear un PROTOTIPO EDUCATIVO completamente simulado.

==================================================
1. CONCEPTO GENERAL
==================================================

FINTRACK 360 debe funcionar como una plataforma ficticia de:

- Consolidación bancaria.
- Historial financiero.
- Historial crediticio.
- Debida diligencia.
- Verificación de identidad simulada.
- Listas y sanciones simuladas.
- Información judicial simulada.
- Información de tránsito simulada.
- Alertas y hallazgos.

Todo debe aparecer dentro de un único perfil 360°.

==================================================
2. DASHBOARD PRINCIPAL
==================================================

Al iniciar sesión mostrar:

PERFIL 360°

Nombre:
Juan Pérez

Documento:
1.000.000.001

Estado general:
VERIFICACIÓN COMPLETADA

Mostrar tarjetas:

[ Bancos ]
5 conectados

[ Cuentas ]
8 cuentas

[ Deuda ]
$15.800.000

[ Créditos ]
4 activos

[ Procesos ]
2 simulados

[ Hallazgos ]
3

[ Score crediticio ]
742 / 900

Debajo:

ACTIVIDAD RECIENTE

- Nuevo movimiento bancario
- Pago de crédito
- Cambio en obligación
- Actualización de proceso judicial simulado
- Nueva alerta

==================================================
3. BANCOS FICTICIOS
==================================================

Crear:

Banco Nova
Banco Andino
Banco Capital
Banco Horizonte
Banco Centralia
Banco Unión
Banco Continental

Cada banco debe tener:

- Cuentas.
- Saldos.
- Movimientos.
- Créditos.
- Tarjetas.
- Obligaciones.
- Historial.

Crear APIs simuladas independientes:

/api/mock/banco-nova
/api/mock/banco-andino
/api/mock/banco-capital
/api/mock/banco-horizonte
/api/mock/banco-centralia

==================================================
4. INFORMACIÓN FINANCIERA
==================================================

Crear una sección:

"Información financiera"

Mostrar:

- Bancos.
- Cuentas.
- Saldos.
- Movimientos.
- Créditos.
- Tarjetas.
- Obligaciones.
- Pagos.
- Mora.

Permitir filtrar por:

- Banco.
- Producto.
- Fecha.
- Estado.

==================================================
5. HISTORIAL CREDITICIO
==================================================

Crear:

"HISTORIAL CREDITICIO"

Mostrar:

Banco
Producto
Tipo
Monto inicial
Saldo
Cuota
Estado
Días de mora

Ejemplo:

Banco Nova
Crédito libre inversión
$15.000.000
Saldo $8.500.000
Estado: Al día

Banco Andino
Tarjeta de crédito
Cupo $8.000.000
Utilizado $5.200.000
Estado: En mora

Crear también:

- Historial de pagos.
- Cuotas.
- Fechas.
- Mora.
- Reestructuraciones.
- Obligaciones cerradas.

==================================================
6. SCORE CREDITICIO SIMULADO
==================================================

Mostrar:

SCORE CREDITICIO

742 / 900

Factores:

- Historial de pagos.
- Endeudamiento.
- Utilización de tarjetas.
- Antigüedad.
- Créditos activos.
- Mora.

IMPORTANTE:

Mostrar siempre:

"Score completamente simulado. No representa un puntaje crediticio real."

==================================================
7. DEBIDA DILIGENCIA
==================================================

Crear una sección:

"Debida diligencia"

Esta sección debe tener una interfaz sencilla con diferentes fuentes simuladas.

Cada fuente debe tener uno de estos estados:

[✓] Sin hallazgos
[!] Hallazgo
[?] No registra
[⚠] Error en fuente
[○] No disponible

NO presentar un error técnico como si fuera un resultado negativo.

Por ejemplo:

OFAC
Estado: Sin hallazgos

Rama Judicial
Estado: 1 coincidencia simulada

PEP
Estado: No registra

Fuente X
Estado: No disponible

==================================================
8. LISTAS VINCULANTES
==================================================

Crear una categoría:

"Listas vinculantes"

Incluir FUENTES SIMULADAS inspiradas en categorías del documento:

- Lista de organizaciones terroristas del Departamento de Estado de EE.UU.
- EU Consolidated Sanctions.
- EU Terrorism Sanctions.
- UN Security Council.
- Otras listas internacionales.

IMPORTANTE:

No consultar las listas reales.

Crear solamente datos simulados.

Ejemplo:

UN Security Council
Estado: Registro no encontrado

EU Sanctions
Estado: Registro no encontrado

==================================================
9. LISTAS RESTRICTIVAS
==================================================

Crear:

"Listas restrictivas"

Con fuentes simuladas como:

- Panama Papers.
- AECA.
- World Bank.
- Comité de Sanciones BID.
- Canadian Autonomous Sanctions.
- BIS Denied Persons.
- BIS Entity List.
- FCPA.
- CAPTA.
- OFAC FSE.
- UK Sanctions.
- OFAC NS-ISA.
- OFAC SDN.
- OFAC Non-SDN.
- OFAC SSI.

Cada fuente debe aparecer como una tarjeta pequeña.

Ejemplo:

OFAC SDN

Estado:
No registra

Última consulta:
03/10/2026

==================================================
10. PEP
==================================================

Crear:

"Personas Expuestas Políticamente (PEP)"

Subcategorías:

- PEP Colombia.
- Relacionados con PEP.
- Funcionarios públicos.
- Altos funcionarios.
- Magistrados.
- Cargos públicos.
- PEP internacionales.

Todos simulados.

Mostrar:

Fuente
Resultado
Fecha de consulta
Estado

==================================================
11. ANTECEDENTES
==================================================

Crear:

"Antecedentes"

Incluir fuentes simuladas:

- Procuraduría.
- Contraloría.
- Contaduría.
- Policía.
- Insolvencias.
- JEPMS.
- Otras fuentes.

Ejemplo:

PROCURADURÍA

Estado:
No registra

CONTRALORÍA

Estado:
No registra

POLICÍA

Estado:
No registra

==================================================
12. INFORMACIÓN JUDICIAL
==================================================

Crear:

"Procesos judiciales"

Incluir:

- Procesos por nombre.
- Procesos por documento.
- Número de proceso.
- Tipo de proceso.
- Juzgado ficticio.
- Ciudad.
- Demandante.
- Demandado.
- Estado.
- Última actuación.
- Fecha.

Crear también:

"Demandas"

"Medidas cautelares"

"Embargos"

"Cobranza jurídica"

Todo debe ser ficticio.

==================================================
13. RAMA JUDICIAL SIMULADA
==================================================

Crear varias jurisdicciones ficticias:

- Bogotá.
- Medellín.
- Cali.
- Barranquilla.
- Cartagena.
- Bucaramanga.
- Pereira.
- Manizales.
- Ibagué.
- Santa Marta.
- Tunja.
- Popayán.
- Armenia.
- Montería.
- Villavicencio.
- Pasto.
- Neiva.
- Valledupar.
- Quibdó.

No realizar consultas reales.

Crear datos ficticios para cada ciudad.

Ejemplo:

MEDELLÍN

Procesos encontrados:
2

BOGOTÁ

Procesos encontrados:
1

CALI

Sin registros simulados.

==================================================
14. TRÁNSITO SIMULADO
==================================================

Crear:

"Información de tránsito"

Mostrar:

- Comparendos.
- Multas.
- Acuerdos de pago.
- Estado.
- Ciudad.
- Fecha.
- Valor.

También simular:

- RUNT.
- SIMIT.
- SIMUR.
- RNDC.

Pero siempre como fuentes ficticias.

==================================================
15. INFORMACIÓN GENERAL
==================================================

Crear:

"Información general"

Mostrar:

- Persona notable SIMULADA.
- Contratación pública SIMULADA.
- Noticias reputacionales SIMULADAS.
- Insolvencias SIMULADAS.
- Información empresarial SIMULADA.

No presentar información inventada como real.

Utilizar siempre la etiqueta:

"DATOS SIMULADOS"

==================================================
16. NOTICIAS Y REPUTACIÓN
==================================================

Crear:

"Noticias reputacionales"

Mostrar noticias ficticias relacionadas con el perfil.

Cada noticia debe tener:

- Título.
- Fecha.
- Fuente ficticia.
- Categoría.
- Nivel.
- Estado.

Ejemplo:

"Empresa ficticia vinculada a investigación administrativa"

Fuente:
Noticias Simuladas

Nivel:
Medio

==================================================
17. SISTEMA DE HALLAZGOS
==================================================

Crear un sistema muy sencillo.

Clasificación:

🔴 ALTO
🟠 MEDIO
🟢 BAJO
⚪ SIN HALLAZGOS

Ejemplo:

ALTO
Proceso judicial simulado activo.

MEDIO
Obligación crediticia simulada en mora.

BAJO
Coincidencia nominal simulada.

SIN HALLAZGOS
No se encontraron registros simulados.

==================================================
18. IMPORTANTE: HOMÓNIMOS
==================================================

El sistema debe distinguir:

"Coincidencia encontrada"

de

"Coincidencia confirmada"

Crear un aviso:

"La coincidencia por nombre puede corresponder a otra persona ficticia con nombres similares."

No asumir automáticamente que una coincidencia nominal pertenece a la persona consultada.

==================================================
19. FUENTES NO DISPONIBLES
==================================================

Cada consulta simulada debe poder devolver:

- Disponible.
- Sin registros.
- Hallazgo.
- Error.
- Indisponible.

Crear una sección:

"Fuentes no disponibles"

Ejemplo:

Banco Horizonte
Estado: Indisponible

Rama Judicial Simulada
Estado: Error de consulta

Esto debe aparecer claramente y NO convertirse automáticamente en "sin hallazgos".

==================================================
20. COMENTARIOS
==================================================

Agregar al final del perfil:

"Comentarios"

Permitir:

- Escribir comentario.
- Guardar.
- Editar.
- Eliminar.

Ejemplo:

"Se recomienda revisar el proceso simulado antes de continuar."

==================================================
21. RECARGAR CONSULTA
==================================================

Agregar botón:

"Actualizar consulta"

Al pulsarlo:

- Volver a ejecutar las APIs simuladas.
- Actualizar estados.
- Registrar fecha de consulta.
- Actualizar el dashboard.

Mostrar:

"Última actualización: 03/10/2026 21:00"

==================================================
22. REPORTE
==================================================

Crear botón:

"Generar reporte"

Debe producir un reporte visual con:

1. Información general.
2. Bancos.
3. Cuentas.
4. Créditos.
5. Historial crediticio.
6. Score.
7. Listas vinculantes.
8. Listas restrictivas.
9. PEP.
10. Antecedentes.
11. Procesos judiciales.
12. Demandas.
13. Medidas cautelares.
14. Tránsito.
15. Noticias.
16. Hallazgos.
17. Fuentes no disponibles.
18. Comentarios.

Agregar:

- Fecha de generación.
- ID ficticio del reporte.
- Estado de consulta.
- Indicador "DATOS SIMULADOS".

==================================================
23. DASHBOARD SIMPLE
==================================================

NO crear una interfaz complicada.

Usar:

Sidebar:

Dashboard
Perfil 360°
Bancos
Cuentas
Créditos
Historial crediticio
Debida diligencia
Listas
PEP
Antecedentes
Judicial
Tránsito
Alertas
Reporte

El usuario debe poder llegar a cualquier información con máximo 2 o 3 clics.

==================================================
24. BUSCADOR
==================================================

Agregar un único buscador global:

"Buscar persona, banco, crédito, proceso..."

Buscar únicamente dentro de la base de datos ficticia.

Resultados:

PERSONA
BANCO
CUENTA
CRÉDITO
PROCESO
DEMANDA
ALERTA
FUENTE

==================================================
25. IA OPCIONAL
==================================================

Agregar un módulo:

"FinTrack AI"

La IA NO debe consultar fuentes por sí misma.

La IA solamente recibe los datos ficticios que ya fueron procesados por el backend.

Debe poder responder preguntas como:

"Resume este perfil."

"¿Cuántos créditos tiene?"

"¿Qué obligaciones están en mora?"

"¿Qué procesos judiciales simulados aparecen?"

"¿Cuáles son los principales hallazgos?"

"Genera un resumen ejecutivo."

La IA debe diferenciar claramente:

DATOS DEL SISTEMA
vs.
INTERPRETACIÓN DE IA

Nunca inventar información que no exista en la base de datos.

==================================================
26. BASE DE DATOS
==================================================

PostgreSQL.

Tablas:

users
banks
accounts
transactions
credits
credit_payments
credit_history
credit_scores
due_diligence_checks
sources
source_results
sanctions
pep_records
background_checks
judicial_processes
lawsuits
legal_measures
traffic_records
reputational_news
alerts
comments
reports
timeline_events

==================================================
27. TECNOLOGÍAS
==================================================

Frontend:

React
TypeScript
Tailwind CSS
Recharts

Backend:

Java
Spring Boot
Spring Security
JWT
REST API

Base de datos:

PostgreSQL

Infraestructura:

Docker
Docker Compose

Herramientas:

Git
GitHub
Postman

==================================================
28. DATOS SIMULADOS
==================================================

Crear:

10 personas ficticias.

7 bancos ficticios.

30 cuentas.

200 transacciones.

20 créditos.

200 pagos.

15 procesos judiciales.

10 demandas.

5 medidas cautelares.

30 alertas.

Múltiples resultados de debida diligencia.

Múltiples resultados de listas.

Múltiples estados de fuentes.

==================================================
29. REGLA DE SEGURIDAD
==================================================

Toda la aplicación debe mostrar claramente:

"PROTOTIPO EDUCATIVO — DATOS COMPLETAMENTE SIMULADOS"

No permitir introducir:

- Documentos reales.
- Contraseñas bancarias.
- Credenciales bancarias.
- Tokens reales.
- Datos financieros reales.

==================================================
30. RESULTADO FINAL
==================================================

Quiero un software funcional y sencillo.

NO quiero solamente una maqueta.

Debe poder:

1. Iniciar sesión.
2. Seleccionar una persona ficticia.
3. Ver su perfil 360°.
4. Ver todos sus bancos ficticios.
5. Ver sus cuentas.
6. Ver transacciones.
7. Ver créditos.
8. Ver historial crediticio.
9. Ver score.
10. Ejecutar debida diligencia simulada.
11. Consultar listas simuladas.
12. Consultar PEP simulado.
13. Consultar antecedentes simulados.
14. Consultar procesos judiciales simulados.
15. Consultar demandas simuladas.
16. Consultar medidas cautelares simuladas.
17. Consultar tránsito simulado.
18. Ver noticias simuladas.
19. Ver alertas.
20. Generar un reporte.
21. Utilizar el asistente FinTrack AI.
22. Actualizar las consultas simuladas.

PRIMERO:

1. Mostrar arquitectura.
2. Mostrar estructura de carpetas.
3. Mostrar modelo de base de datos.
4. Mostrar relaciones.
5. Mostrar endpoints.
6. Mostrar flujo de información.
7. Después generar el código completo.

NO omitir archivos.
NO utilizar pseudocódigo cuando se solicite código.
NO conectar fuentes reales.
NO inventar que una fuente real fue consultada.
