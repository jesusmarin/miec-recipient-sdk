# SDK-03, paso 1: contrato de comportamiento

Estado: especificación definida; las firmas de este documento se implementan en
el paso 2 y las guardas de la fachada en el paso 3. No son APIs disponibles aún.
Proyecto responsable de este paso: miec-recipient-sdk.

## Ejecución

Interfaz prevista: com.miec.recipient.spi.OperationExecutor.

```java
OperationResult execute(OperationRequest request);
```

La entrada existente contiene clave estable, destino, tipo, cuerpo exacto y huella
calculada. El consumidor autentica la llamada y valida su dominio. El SDK no
introduce permisos, planes, entidades ni repositorios de MIEC en esa entrada.
Los metadatos de transporte adicionales permanecen en el adaptador; transmitir
un token no constituye evidencia de fencing en el destinatario.

Una respuesta de ejecución debe corresponder a la clave, destino y huella recibidos:

| Respuesta | Regla |
|---|---|
| APPLIED | Referencia opaca obligatoria; no acredita por sí sola autenticidad ni autorización de una decisión de MIEC. |
| UNKNOWN | Sin referencia; la huella corresponde al contexto conocido de la solicitud, no a un efecto confirmado. |
| NOT_FOUND | No es respuesta válida de execute: la ausencia visible pertenece a consulta y no prueba que el envío careció de efecto. |
| null | Error de contrato de salida. |

La implementación no debe crear una clave distinta en cada reintento. Si implementa
deduplicación, una clave con otro contenido genera OPERATION_CONFLICT. Ese conflicto
no demuestra ausencia de un efecto previo. No se incluyen reintentos automáticos.

## Consulta sin cuerpo de negocio

Interfaz prevista: com.miec.recipient.spi.OperationQuery.

```java
OperationQueryResult lookup(OperationQueryRequest request);
```

Se definen dos modelos nuevos para el paso 2:

| Modelo/campo | Contrato |
|---|---|
| OperationQueryRequest.operationKey | UUID obligatorio de la operación consultada. |
| OperationQueryRequest.destinationId | Destino lógico obligatorio; mismas reglas de normalización que OperationPayload. |
| OperationQueryRequest.expectedFingerprint | Optional<String>, vacío si el llamador no conoce la huella; si existe, debe ser hexadecimal canónico. No sustituye el resultado observado. |
| OperationQueryResult.operationKey | Misma clave consultada. |
| OperationQueryResult.destinationId | Mismo destino consultado. |
| OperationQueryResult.status | APPLIED, NOT_FOUND o UNKNOWN. |
| OperationQueryResult.appliedResult | Optional<OperationResult>; obligatorio solo en APPLIED. |

Los Optional nunca son null. El request tendrá una construcción abreviada con clave
y destino, sin huella esperada, y una conversión desde OperationRequest que conserva
su huella cuando el contexto la conoce. No debe enviarse el cuerpo para consultar.

En APPLIED, la confirmación es un OperationResult de estado APPLIED, con igual clave
y destino, huella válida y referencia obligatoria. Si la consulta aportó una huella
esperada, la fachada también comprueba su coincidencia. Si no la aportó, la respuesta
identifica el contenido observado, pero no demuestra su correspondencia con un
cuerpo desconocido por el llamador.

NOT_FOUND y UNKNOWN llevan Optional.empty como confirmación. No se crean huellas
vacías, de ceros o ficticias. OperationResult conserva sus invariantes actuales y
puede seguir representando estados inciertos cuando ya se conoce la solicitud.
La nueva envoltura resuelve específicamente la consulta sin contenido original.

Consultar no invoca el ejecutor, no reserva una clave ni ejecuta efectos del negocio.
Un resultado ausente no descarta una operación en vuelo. Un resultado incierto no
habilita reenvíos ni se convierte en NO_EFFECT_CONFIRMED.

## Requisitos y capacidades

El paso 3 implementará OperationRequirements y RecipientContract. La fachada se
configura en el servidor con destino lógico, capacidades, requisitos de ejecución
y los puertos disponibles. La solicitud no puede sustituirlos ni reducirlos.

| Ruta | Requisitos |
|---|---|
| Ejecución básica | El conjunto configurado puede estar vacío. No promete deduplicación, atomicidad o recuperación. |
| Consulta | Siempre exige LOOKUP y un puerto de consulta instalado; no exige cuerpo ni persistencia por sí sola. |
| Ejecución protegida de software | Exige LOOKUP, DURABLE_DEDUPLICATION, ATOMIC_EFFECT_RESULT y RECOVERY_AFTER_RESTART, con sus puertos necesarios instalados. |

Los requisitos se comprueban antes de delegar. La entrada debe corresponder al
destino instalado. Los requisitos de ejecución no impiden consultar un destinatario
que sí soporta LOOKUP: observar no equivale a autorizar un nuevo efecto.
La coherencia de la declaración no prueba sus garantías; el consumidor las demuestra
con persistencia y pruebas apropiadas. La fachada no es una barrera contra código
Java malicioso ni reemplaza autenticación y autorización de las aplicaciones.

## Errores y propagación

| Código | Uso |
|---|---|
| INVALID_REQUEST | Entrada o configuración inválida según el modelo correspondiente. |
| FINGERPRINT_MISMATCH | Huella declarada distinta del snapshot capturado. |
| OPERATION_CONFLICT | El destinatario reconoce la clave con otro contenido. |
| UNSUPPORTED_CAPABILITY | Ruta o puerto no disponible para los requisitos instalados; rechazo antes de delegar. |
| UNAUTHORIZED | Rechazo de autenticación o autorización informado por una integración. |
| TEMPORARILY_UNAVAILABLE | Consulta o envío sin confirmación disponible. No prueba ausencia de efecto. |
| INVALID_RESULT, nuevo en el paso 2 | La fachada detecta salida nula, estado no permitido o identidad/huella distintos de lo esperado. |

Los errores de construcción de modelos existentes mantienen sus códigos actuales.
INVALID_RESULT se utiliza al validar la salida de un puerto; no se cambia el
significado de INVALID_REQUEST ni se clasifica un fallo de autenticación como una
respuesta inválida. Una excepción del puerto se propaga sin convertirla en APPLIED,
NOT_FOUND o ausencia confirmada, y sin invocar automáticamente otra vez el puerto.
Los mensajes de error no deben incorporar cuerpos, secretos ni consultas SQL.

El adaptador de transporte decide la traducción HTTP. No se expone una excepción
SQL directamente como contrato de negocio. El coordinador de MIEC conserva su
tratamiento de incertidumbre, permisos, propietario y reconciliación.

## Bindings de los consumidores

| Consumidor | Ejecución | Consulta |
|---|---|---|
| PetClinic | El binding POST construye OperationRequest y delega con su contexto autenticado al servicio de visitas. La transacción sigue en PetClinic. | GET /miec/operations/{key} usa clave y destino configurado, sin cuerpo. El binding traduce ausencia a la semántica HTTP existente; no necesita fabricar huella. |
| Puente MIEC de test | Conserva el OperationRequest derivado del snapshot persistido y exige el perfil protegido. | Conoce la huella esperada y la incluye en la consulta lógica; valida la respuesta aunque el GET HTTP no transporte esa huella. |
| Reservas, revisión de diseño | El adaptador interpreta units sin agregarlo al SDK. | El ledger del consumidor determina la confirmación; no es almacenamiento administrado por el SDK. |

Las envolturas Java no obligan a modificar los endpoints ni a serializarlas
literalmente como JSON. El adaptador realiza esa traducción. La aplicación de
reservas todavía no se adapta en este paso: únicamente se comprueba la generalidad
del diseño. Simulaciones o dispositivos físicos requieren sus propios bindings y
no heredan garantías transaccionales de las pruebas con software.

## Aceptación de este paso

- Ejecución y consulta tienen entradas, salidas y errores explícitos.
- Consulta sin cuerpo representa ausencia e incertidumbre sin huella inventada.
- OperationResult existente conserva su contrato.
- Perfiles básico y protegido permiten persistencia opcional sin prometer garantías inexistentes.
- Visitas y reservas usan el mismo vocabulario, sin entidades de dominio en el SDK.
- El binding GET actual puede conservarse.
- Los escenarios del documento SDK-03-validation-cases.md identifican qué debe
  comprobarse al implementar los pasos 2 y 3. No se presentan como pruebas ya ejecutadas.