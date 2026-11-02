# Contrato de integración del destinatario — versión lógica 1

## Alcance

El SDK define datos y puntos de extensión para que un destinatario implemente el
contrato de MIEC. No importa miec-prototype, no ejecuta negocio por sí mismo, no
inicia un servidor y no persiste datos. La aplicación administra sus datos y puede
implementar persistencia propia o añadir un proveedor opcional en otro artefacto.

Los modelos son independientes de visitas, reservas y otras disciplinas. En esta
entrega se valida integración de software; una actuación física no hereda las
propiedades de una transacción SQL. La biblioteca no representa todo el proyecto.

## Identidad y solicitud

- operationKey: UUID estable asignado por el coordinador para una operación lógica.
  Reintentar no cambia esta clave. Una solicitud distinta exige otra operación.
- destinationId: identidad lógica del destinatario, no una URL ni una credencial.
  Se recortan espacios ASCII periféricos según String.trim; máximo 255 unidades
  UTF-16 después del recorte. No se convierte a minúsculas.
- mediaType: texto obligatorio, recortado y convertido a minúsculas con Locale.ROOT;
  máximo 255 unidades UTF-16. Es una etiqueta acordada, no un parser completo MIME.
- body: texto UTF-8 exacto, máximo 1 048 576 bytes; puede estar vacío. No se reordena
  JSON ni se elimina whitespace. Se rechaza UTF-16 mal formado para evitar que dos
  textos distintos se conviertan silenciosamente al mismo carácter de sustitución.
- fingerprint: SHA-256, 64 caracteres hexadecimales en minúsculas, calculado por el
  SDK; no se confía en una huella recibida sin compararla con la solicitud.

OperationPayload captura destino, tipo y cuerpo. OperationRequest añade la clave.
Su huella se deriva del payload; no se acepta una huella mutable como atributo.
La verificación de una huella declarada debe ocurrir antes de ejecutar el negocio.
El transporte entrega los bytes capturados por bodyBytes(), sin reconstruirlos.

## Representación UTF8-V1

El resumen se calcula concatenando estos cuatro campos en orden:

1. UTF8-V1
2. destinationId normalizado
3. mediaType normalizado
4. body exacto

Cada campo lleva primero su longitud UTF-8 como entero de 32 bits, big endian,
seguida de sus bytes UTF-8. El resultado SHA-256 se representa en hexadecimal
minúsculo. Esta representación es compatible con ExecutionRequest de MIEC para
texto Unicode válido. El SDK rechaza además Strings Java con sustitutos Unicode
sin pareja; esa validación es más estricta que la implementación actual de MIEC.
La versión de esta representación es distinta de la versión Maven del artefacto.

## Resultado de consulta

OperationResult vincula operationKey, destinationId y fingerprint con un estado:

| Estado | Significado | Referencia |
|---|---|---|
| APPLIED | El destinatario confirma el efecto correspondiente a la operación. | Obligatoria, opaca, recortada, máximo 500 unidades UTF-16. |
| NOT_FOUND | La consulta no ve un resultado confirmado. Puede haber trabajo en vuelo. | Debe ser null. |
| UNKNOWN | El destinatario no puede confirmar el resultado. | Debe ser null. |

Una respuesta solo se acepta si corresponde a la clave, destino y huella esperados.
APPLIED no significa que MIEC haya verificado autenticidad o autorizado una decisión;
la fuente configurada y la reconciliación siguen teniendo esas responsabilidades.
NOT_FOUND no es NO_EFFECT_CONFIRMED; UNKNOWN no autoriza reintentar automáticamente.
No se introduce FAILED como sinónimo de ausencia de efecto.

La referencia no se interpreta como una URL ni se sigue automáticamente. El modelo
no impone un timestamp remoto: la fecha del hecho y la de su observación tienen
significados diferentes y no gobiernan por sí solas el lease de MIEC.

## Errores y garantías

INVALID_REQUEST indica datos fuera del contrato. FINGERPRINT_MISMATCH indica que la
huella declarada no corresponde a la solicitud. OPERATION_CONFLICT representa una
clave utilizada con otro contenido. UNSUPPORTED_CAPABILITY indica una ruta no
compatible con las capacidades instaladas. UNAUTHORIZED y TEMPORARILY_UNAVAILABLE
se reservan para implementaciones futuras del transporte y los puntos de extensión.
Estos códigos no convierten un error de envío en prueba de ausencia de efecto.

Los consumidores no deben incluir secretos ni contenido sensible en mensajes de
error. Las validaciones del SDK usan mensajes genéricos. Autenticación, autorización,
endpoints, serialización y traducción a HTTP pertenecen a integraciones posteriores;
este contrato describe campos lógicos y no cambia aún los endpoints de PetClinic.

Si se declara deduplicación durable, la misma clave y contenido deben conservar el
mismo resultado después de reiniciar. Cambiar el contenido se rechaza. Si se declara
atomicidad, clave, efecto y resultado deben confirmarse juntos. Persistir solo la
clave en una transacción independiente no cumple esa garantía.

## Revisión con dominios de referencia

| Destinatario | Cuerpo específico del adaptador | Referencia opaca de ejemplo |
|---|---|---|
| PetClinic | ownerId, petId, date, description | petclinic:visit:123 |
| Reservas | units y la identidad de inventario que defina el consumidor | reservation:123 |
| Simulación eléctrica, solo revisión conceptual | Identidad y versión del modelo, parámetros y unidades explícitas | simulation:run:123 |

El SDK no valida esas entidades ni unidades: el consumidor valida su dominio. Las
mismas identidades, huellas y estados permiten representar los tres escenarios.
Cambiar parámetros, versión del modelo o unidades cambia los bytes del cuerpo y
su huella. Esto no demuestra idempotencia de un dispositivo eléctrico real.

## Evolución

La primera versión es experimental 0.1.0-SNAPSHOT. Los modelos Java no constituyen
por sí solos un protocolo HTTP interoperable. Los bindings y pruebas de consumidores
se completarán en los pasos siguientes. No se modifican las rutas actuales de MIEC
ni se publican garantías productivas por el hecho de agregar esta dependencia.