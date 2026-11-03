# SDK-03: escenarios derivados del contrato

Estos son criterios de diseño y pruebas futuras. El paso 1 los revisa contra los
modelos existentes; no declara implementadas interfaces, guardas ni nuevas pruebas.

| ID | Escenario | Resultado esperado | Validación ejecutable prevista |
|---|---|---|---|
| Q01 | Consulta con clave y destino, sin cuerpo ni huella esperada. | Entrada válida, sin reconstruir OperationPayload. | Paso 2, modelos de consulta. |
| Q02 | Consulta APPLIED. | Confirmación presente con clave/destino iguales y huella/referencia válidas. | Pasos 2 y 3. |
| Q03 | Consulta NOT_FOUND. | Confirmación vacía; no se inventa huella ni se acredita NO_EFFECT_CONFIRMED. | Pasos 2 y 3. |
| Q04 | Consulta UNKNOWN. | Confirmación vacía; no hay reintento automático. | Pasos 2 y 3. |
| Q05 | Huella esperada conocida y confirmación con otra huella. | Rechazo INVALID_RESULT antes de aceptar la confirmación. | Paso 3. |
| Q06 | Consulta sin LOOKUP o sin puerto instalado. | Rechazo UNSUPPORTED_CAPABILITY; cero llamadas al ejecutor y al puerto no admitido. | Paso 3. |
| Q07 | Consulta válida sobre una configuración que no permite ejecutar. | Puede observarse sin conceder permiso para un efecto nuevo. | Paso 3. |
| E01 | Ejecución APPLIED con identidad y huella esperadas. | Referencia válida y resultado aceptado; no se inventa una segunda llamada. | Pasos 2 y 3. |
| E02 | Ejecución UNKNOWN con contexto conocido. | Referencia ausente e incertidumbre conservada. | Paso 3. |
| E03 | execute devuelve NOT_FOUND o null. | Rechazo INVALID_RESULT; no implica ausencia de efecto. | Paso 3. |
| E04 | Una salida usa clave o destino ajenos. | Rechazo INVALID_RESULT. | Paso 3. |
| E05 | Perfil básico sin capacidades durables. | Entrada admitida si cumple sus requisitos; no se declara durabilidad. | Paso 3. |
| E06 | Perfil protegido sin uno de sus compromisos. | Rechazo previo a delegar; cero efectos. | Paso 3. |
| E07 | Conflicto de una clave existente con otro contenido. | OPERATION_CONFLICT; no se considera que no hubo efecto anterior. | Pasos 3 y 5. |
| F01 | Puerto lanza UNAUTHORIZED o TEMPORARILY_UNAVAILABLE. | Se conserva el error; no se fabrica resultado ni se reenvía. | Paso 3. |
| C01 | Colecciones de capacidades/requisitos o Optional inválidos. | Rechazo o copia defensiva; configuración inmutable. | Pasos 2 y 3. |
| D01 | Implementadores de visitas y reservas. | Compilan con las mismas interfaces sin dependencias de entidades externas. | Paso 2; son implementaciones mínimas, no consumidores desplegados. |
| I01 | GET PetClinic con clave desconocida. | Mantiene el HTTP de ausencia existente y el cliente lo mapea a NOT_FOUND. | Pasos 5 y 7. |
| I02 | Respuesta perdida, confirmación y otra JVM. | Mismo resultado y un solo efecto durable; garantías de la implementación real. | Paso 7. |

## Revisión de diseño realizada en el paso 1

- Q01/Q03/Q04: la nueva envoltura deja la confirmación vacía y evita debilitar el
  requisito de huella del OperationResult existente.
- E05/E06: los requisitos se instalan en servidor y distinguen uso básico de uso
  protegido. Ningún dato de una solicitud reduce el perfil configurado.
- D01/I01: visitas y reservas solo aportan su payload; el GET de PetClinic sigue
  requiriendo clave y contexto de servidor, no el cuerpo original.
- La fachada validará identidad antes de aceptar salidas, pero no puede deshacer
  efectos que ya realizó un puerto. INVALID_RESULT conserva incertidumbre sobre
  el efecto; no significa rollback.
- Se conserva la representación UTF8-V1 y las validaciones de SDK-01/SDK-02.

La evidencia de este paso consiste en esta revisión y la especificación normativa.
La suite existente de 24 pruebas verifica modelos ya implementados; no acredita los
escenarios futuros de las interfaces. Sus nuevas pruebas se explicarán antes de
escribirlas en los pasos correspondientes.