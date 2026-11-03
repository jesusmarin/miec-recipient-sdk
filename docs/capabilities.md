# Matriz de capacidades del destinatario

Las capacidades describen compromisos de una implementación instalada. Se configuran
en el servidor y se respaldan con pruebas; no se aceptan como hechos porque el
llamador las envíe dentro de una solicitud. El SDK base no las habilita al persistir
ni implementa automáticamente el comportamiento que representan.

| Capacidad | Compromiso | Condiciones |
|---|---|---|
| LOOKUP | Consultar estado sin ejecutar el efecto. | No implica deduplicación ni persistencia. |
| DURABLE_DEDUPLICATION | Misma clave y contenido conservan resultado durable, con rechazo de conflictos. | El destinatario necesita almacenamiento durable propio o un proveedor opcional. |
| ATOMIC_EFFECT_RESULT | Clave, efecto y resultado se confirman o revierten juntos. | Requiere DURABLE_DEDUPLICATION y coordinación transaccional real. |
| RECOVERY_AFTER_RESTART | Consultar el resultado conservado después de reiniciar. | Requiere LOOKUP y DURABLE_DEDUPLICATION. |

ATOMIC_EFFECT_RESULT no se infiere de RECOVERY_AFTER_RESTART ni al revés. La atomicidad
solo puede declararse para efectos que la implementación coordine realmente. El
fencing del destinatario no está incluido en estas capacidades iniciales.

## Perfiles y decisiones

| Uso | Capacidades necesarias | Regla |
|---|---|---|
| Solo representar datos del protocolo | Ninguna. | El JAR no impone una base de datos. |
| Consultar un destinatario | LOOKUP. | NOT_FOUND y UNKNOWN no prueban ausencia de efecto. |
| Ruta protegida de software con reintento incierto y efecto transaccional | Las cuatro capacidades. | Se exige demostrar deduplicación, atomicidad y recuperación. |
| Operación sin almacenamiento durable | Puede declarar LOOKUP si lo implementa. | No se admite como destinatario de la ruta protegida anterior. |
| Actuación física o infraestructura no transaccional | No cubierta por ese perfil. | Definir otro protocolo, evidencia y control de efectos antes de habilitar reintentos. |

La ruta protegida actual de MIEC presupone garantías del adaptador; todavía no
consume estos nuevos modelos de capacidades. Su comprobación antes del envío
corresponde a SDK-03/SDK-06. Construir RecipientCapabilities valida coherencia de la
declaración, pero no prueba el comportamiento de una aplicación externa.

## Pruebas exigidas por capacidad

- LOOKUP: consulta sin efecto, resultados ligados a identidad/huella y manejo de
  incertidumbre o indisponibilidad.
- DURABLE_DEDUPLICATION: replay, conflicto de contenido y concurrencia con conteo
  de efectos reales; reapertura del almacenamiento.
- ATOMIC_EFFECT_RESULT: fallo antes y después del efecto, rollback conjunto, y
  ausencia de resultados parciales visibles como confirmados.
- RECOVERY_AFTER_RESTART: otra JVM conserva resultado y referencia; la prueba no
  acredita por sí sola tolerancia a pérdida de energía del equipo.

PetClinic ya aporta evidencia experimental entre procesos en el repositorio MIEC.
Reservas aporta un segundo dominio y pruebas locales de ledger. PetClinic ya incorpora
el JAR del SDK para modelos y validaciones, con persistencia propia; el adaptador
de MIEC usa el mismo contrato en scope test. Reservas todavía no consume el JAR.
La aplicación de la matriz mediante interfaces SDK-03 sigue pendiente.

## Persistencia opcional

El SDK base no tiene driver SQL, repositorios, tablas ni scripts. Un consumidor puede
implementar el registro con su infraestructura. Un futuro miec-recipient-jdbc será
otra dependencia, seleccionada explícitamente y sin activación automática.
No se ofrece un proveedor en memoria como reemplazo silencioso de durabilidad.