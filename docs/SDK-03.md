# SDK-03: interfaces de ejecución y consulta

Estado: paso 1 completado (especificación de comportamiento). Pasos 2 a 8 pendientes.

## Objetivo

Pasar de compartir modelos y validaciones a un contrato de comportamiento que
puedan implementar distintos destinatarios. La mejora principal pertenece a
miec-recipient-sdk. PetClinic será el primer implementador y miec-prototype
comprobará su funcionamiento mediante el adaptador experimental de test.

La entrega anterior validó los modelos del JAR con PetClinic. No implementó todavía
estos puntos de extensión ni la aplicación de requisitos de capacidades.

## Distribución entre proyectos

| Proyecto | Cambios previstos | Responsabilidad que conserva |
|---|---|---|
| miec-recipient-sdk | Interfaces, validación común de comportamiento, requisitos configurables y pruebas unitarias. | Independencia de framework, transporte, SQL y entidades de consumidores. |
| spring-petclinic | Implementar los puntos de extensión en el paquete experimental miec, delegando el negocio al servicio de visitas. | Autenticación, endpoints, validación de visitas, tablas y transacción propia. |
| miec-prototype | Adaptar el puente de test y verificar extremo a extremo con el JAR actualizado. | Autorización, propiedad, vigencia, intentos, evidencia y reconciliación. |

PetClinic se modifica en miec/experimento-001. En MIEC, la dependencia del SDK y los
adaptadores PetClinic permanecen en test. No se propone trasladar en este incremento
los servicios centrales de MIEC al SDK ni incluir el experimento en producción.

## Secuencia de implementación

### 1. Precisar el contrato de comportamiento

Proyecto: miec-recipient-sdk.

Definir responsabilidades, entradas, salidas y errores de ejecución y consulta.
Separar identidad de consulta de contenido de ejecución: consultar por una clave
no debe exigir reenviar el cuerpo de negocio. Acordar cómo representar ausencia
o incertidumbre cuando todavía no existe una huella confirmada; no inventar una
huella para satisfacer un constructor. OperationResult mantiene su validación.

Distinguir los requisitos de una operación sencilla de los del perfil protegido
de software de MIEC. No imponer almacenamiento a todos los consumidores.

Validación: representar visitas y reservas sin entidades particulares; preservar
la consulta GET actual de PetClinic y la semántica NOT_FOUND/UNKNOWN.

### 2. Implementar los puntos de extensión

Proyecto: miec-recipient-sdk.

Crear interfaces separadas de ejecución y consulta, con nombres previstos
OperationExecutor y OperationQuery, dentro de com.miec.recipient.spi. Los modelos
adicionales de consulta, si se justifican en el paso 1, pertenecen a api. Documentar
cada método y sus responsabilidades de efectos, autenticación y errores.

Validación: implementaciones mínimas de dos dominios compilan utilizando solo SDK
y JDK 17. El SDK no abre conexiones, inicia servidores ni introduce Spring.

### 3. Implementar requisitos y guardas comunes

Proyecto: miec-recipient-sdk.

Crear una fachada pequeña, con nombres previstos RecipientContract y
OperationRequirements, para validar capacidades requeridas antes de delegar, y
comprobar identidad y coherencia de los resultados devueltos. Los nombres son de
planificación y se consolidan con el diseño del paso 1.

Capacidades y requisitos se instalan mediante configuración confiable del servidor;
no se rebajan desde parámetros de una solicitud externa. El perfil protegido de
software requiere consulta, deduplicación durable, atomicidad y recuperación tras
reinicio. Otros usos pueden configurar requisitos distintos. La fachada comprueba
compromisos declarados; las pruebas del consumidor demuestran su cumplimiento.

No añadir reintentos automáticos, persistencia implícita, autenticación inventada
ni equivalencias entre NOT_FOUND y ausencia confirmada de efecto. No convertir
una excepción posterior al envío en un resultado aplicado o sin efecto.

### 4. Validar y empaquetar el SDK

Proyecto: miec-recipient-sdk.

Antes de escribir pruebas, explicar los escenarios. Cubrir al menos:

- Capacidad insuficiente: rechazo previo a invocar el ejecutor o producir efectos.
- Consulta: no invoca el ejecutor; representa ausencia e incertidumbre correctamente.
- Resultado: rechazo de clave, destino o huella ajenos cuando la expectativa esté disponible.
- Excepciones: conservar la incertidumbre, sin reenvío ni resultado inventado.
- Perfil sin persistencia: utilizable para los casos que no requieren durabilidad.
- Perfil protegido: exige todos sus compromisos, sin activarlos por declararlos.
- Dos implementaciones de dominio utilizan las mismas interfaces y validaciones.

Ejecutar la suite completa, preservar los vectores UTF8-V1 y verificar JAR Java 17,
solo dependencias JDK y exclusión de clases de pruebas. Instalar en Maven local;
los consumidores deben utilizar el mismo repositorio local.

Criterio de avance: todas las pruebas del SDK y revisión del artefacto aprobadas.

### 5. Implementar el contrato en PetClinic

Proyecto: spring-petclinic, rama miec/experimento-001.

Agregar las implementaciones de las interfaces en su paquete experimental. La
operación de visitas sigue delegando en la transacción existente que guarda clave,
visita y resultado. La integración HTTP traduce los contratos sin trasladar a la
biblioteca las reglas de mascotas, propietarios, fechas ni el esquema de datos.

Mantener la autenticación y las comprobaciones de metadatos del transporte, incluido
el tratamiento actual del token; ese transporte no demuestra fencing distribuido.
Preservar los endpoints existentes o documentar explícitamente cualquier cambio.

Validación: las seis pruebas HTTP actuales y los casos nuevos de uso de interfaces,
además de regresión sin contenedores. Confirmar replay, conflicto, concurrencia,
rollback y que el JAR contiene el SDK actualizado. No usar instancias o bases del
usuario para las pruebas aisladas.

Criterio de avance: PetClinic implementa y usa el contrato de comportamiento, sin
perder garantías ni duplicar sus validaciones generales innecesariamente.

### 6. Adaptar el puente experimental de MIEC

Proyecto: miec-prototype, src/test/java/com/miec/experiment/petclinic.

Mapear el contrato externo al puerto interno IdempotentActionExecutor, sin forzar
que ambos tipos sean idénticos. Configurar los requisitos del perfil protegido en
el puente que invoca el destinatario y verificar su rechazo previo al envío cuando
no se satisfacen. Las declaraciones de capacidad no se aceptan como prueba remota.

Conservar la selección confiable de fuente de evidencia. La autorización, propiedad,
lease y reconciliación de MIEC siguen bajo sus servicios productivos existentes.
IdempotentActionExecutionService no necesita modificarse solo para incorporar
nombres de interfaces externas.

Validación: pruebas del adaptador y de verificación, y control de que SDK y clases
PetClinic siguen fuera del JAR productivo de MIEC.

### 7. Ejecutar regresiones y pruebas entre procesos

Proyectos: los tres.

Orden: construir e instalar SDK, construir PetClinic, validar MIEC y ejecutar sus
IT explícitos contra el nuevo JAR PetClinic. Registrar los resultados por proyecto.
La base de referencia es SDK 24, PetClinic 83 sin contenedores, MIEC 688 y cinco IT
explícitos; el incremento añadirá las pruebas de comportamiento necesarias, por lo
que los totales podrán aumentar.

Comprobar respuesta perdida después del commit, resultado ligado a la operación,
verificación, reconciliación y reinicio en otra JVM con un solo efecto durable.
Conservar el manejo acotado del bloqueo H2 de Windows y no reintentar aserciones.
No afirmar cobertura PostgreSQL/MySQL si no se ejecutan sus pruebas reales.

### 8. Documentar y registrar el incremento

Proyectos: los tres, según sus cambios.

Actualizar contratos, capacidades, guía de implementación y progreso. Identificar
qué quedó implementado y qué sigue pendiente. Agregar archivos nuevos al índice
Git del repositorio correspondiente; no crear commits ni publicar remotamente sin
instrucción. Excluir artefactos, credenciales y bases locales del incremento.

## Condición de finalización de SDK-03

- SDK ofrece ejecución y consulta extensibles con requisitos y validaciones comunes.
- PetClinic implementa y utiliza esas interfaces, además de los modelos actuales.
- El puente experimental de MIEC valida el perfil protegido y mantiene sus protecciones.
- Pruebas unitarias, regresiones y escenarios entre JVM aprobados y documentados.
- Persistencia continúa siendo elección del destinatario y no una obligación del JAR.
- La validación no se presenta como cierre completo de IR-009 productivo.

## Fuera del incremento

Proveedor JDBC opcional, starter Spring Boot, publicación remota y módulo distribuible
de testkit. El segundo consumidor real del JAR, reservas, permanece como incremento
posterior; los dos dominios usados en pruebas unitarias no equivalen a desplegar un
segundo destinatario. Modelos eléctricos y efectos físicos no se implementan aún.

El primer trabajo ejecutable es el paso 1 en miec-recipient-sdk. Este documento no
modifica por sí mismo las interfaces ni el comportamiento de los tres proyectos.
## Evidencia del paso 1

- [Contrato de comportamiento](SDK-03-contract.md).
- [Escenarios y criterios de aceptación](SDK-03-validation-cases.md).

La consulta se separó del payload para no exigir cuerpo ni huella desconocida.
Se definieron los modelos previstos, las reglas de salida y los perfiles. No se
modificaron las APIs Java actuales ni los consumidores. Conforme a la instrucción
de trabajar paso por paso, el proceso se detiene aquí, antes del paso 2.