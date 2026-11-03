# Ruta de desarrollo de miec-recipient-sdk

## Objetivo y alcance

Construir una biblioteca Java independiente para integrar destinatarios con MIEC.
La primera entrega se valida con proyectos de software. Los contratos deben usar
conceptos generales (destino, operación, solicitud, resultado y evidencia), sin
incorporar visitas, inventario ni entidades internas de miec-prototype.

MIEC también puede coordinar proyectos de otras áreas, incluidos modelos eléctricos,
simulaciones o desarrollo de objetos. El SDK será una frontera de integración con
sus herramientas o sistemas ejecutores; no pretende representar todo el proyecto
ni resolver su disciplina. Un modelo eléctrico implementado en software puede usar
los contratos. Actuar sobre un objeto físico requiere garantías y adaptadores que
no se acreditan con las pruebas de bases de datos de esta primera entrega.

## Distribución de responsabilidades

| Componente | Responsabilidad |
|---|---|
| MIEC Prototype | Planificación, autorización, intentos, propiedad, vigencia, evidencia y reconciliación. |
| SDK base | Modelos, interfaces, validaciones y semántica compartida del protocolo. |
| Aplicación destinataria | Operación de negocio o del dominio, autenticación de entrada y cumplimiento de las garantías que declara. |
| Persistencia propia u opcional | Registro durable de claves y resultados cuando se necesita; no sustituye los datos de negocio ni una transacción compartida. |
| Adaptador | Traducción entre el protocolo de MIEC y el sistema o herramienta del destinatario. |

El SDK base no crea tablas ni abre conexiones, no inicia un servidor, no configura
credenciales y no introduce dependencias de Spring, PetClinic o miec-prototype.

## Principios acordados

- Java 17, JAR convencional y versión inicial 0.1.0-SNAPSHOT.
- Persistencia opcional: el consumidor puede usar sus repositorios o incorporar
  una implementación adicional. No hay almacenamiento en memoria activado como
  sustituto silencioso de un registro durable.
- Las capacidades se configuran en el servidor y se demuestran con pruebas; no
  se aceptan como una declaración confiable aportada por cada solicitud.
- Consulta disponible, deduplicación durable y atomicidad son garantías distintas.
  No persistir puede ser válido en ciertos usos, pero no acredita recuperación
  idempotente después de reinicios ni habilita por sí solo reintentos inciertos.
- Una respuesta perdida mantiene incertidumbre. NOT_FOUND no demuestra que no
  haya un efecto en vuelo; UNKNOWN no autoriza un reintento automático.
- La biblioteca comparte el contrato de integración, no las entidades de gestión
  interna de MIEC. Cada destinatario conserva el significado de sus resultados.

## Secuencia y criterios de aceptación

### SDK-01. Especificación y matriz de capacidades

Definir vocabulario, responsabilidades, formato y versión del protocolo, estados,
conflictos, retención y significado de la evidencia. Acordar qué capacidades exige
cada ruta de MIEC y qué ocurre si el destino no las implementa. Diferenciar una
operación consultable de una operación con deduplicación durable y efecto atómico.

Validación: representar visitas y reservas con el mismo contrato; describir como
caso de diseño una ejecución de simulación eléctrica sin agregar entidades de
ese dominio al SDK. Documentar límites, sin implementar aún otra área.

### SDK-02. Modelos y validaciones independientes

Implementar identidad de operación, solicitud inmutable, estado, resultado y errores
comunes. Incluir cálculo de huella y versión de representación. Separar la versión
del protocolo de la versión del artefacto Maven.

Validación: vectores compartidos con UTF8-V1 de MIEC, Unicode, bytes exactos,
cambios de contenido, límites e identidades inválidas. Verificar compatibilidad
binaria Java 17 y ausencia de dependencias productivas de los consumidores.

### SDK-03. Interfaces de ejecución y consulta

Definir los puntos de extensión para implementar una operación de dominio y consultar
su resultado sin volver a ejecutarla. Mantener los detalles de HTTP y autenticación
fuera del contrato base. Precisar que transportar un fencing token no demuestra
que un sistema externo lo aplique.

Validación: implementaciones de prueba con dominios diferentes; consulta sin efecto,
resultado aplicado, conflicto e incertidumbre. Ninguna interfaz exige clases de
planificación, autorizaciones o persistencia internas de MIEC.

### SDK-04. Extensión de persistencia y modelo transaccional

Definir el puerto opcional del registro idempotente y su ciclo de vida. Especificar
reserva concurrente, resultado, retención y participación en la transacción del
efecto. El consumidor puede implementarlo con sus propios repositorios.

Validación: demostrar que el SDK base funciona sin un proveedor de almacenamiento;
rechazar una ruta que exige garantías durables si no hay una implementación válida.
No crear tablas ni confirmar una transacción ajena. Para efectos que no pueden
compartir una transacción de base de datos, definir un protocolo específico antes
de prometer atomicidad; un registro SQL aislado no resuelve un efecto físico.

### SDK-05. Pruebas reutilizables de cumplimiento

Crear un soporte de pruebas separado del JAR productivo. Exigir al arnés de cada
consumidor escenarios aislados y observación del efecto real. Separar las pruebas
comunes de consulta, replay, conflicto y concurrencia de las pruebas de durabilidad,
rollback y reinicio que requieren infraestructura del destinatario.

Validación: el mismo conjunto se aplica a dos implementaciones sin copiar pruebas
ni exigir garantías que no se han declarado. Los resultados indican qué capacidades
se comprobaron y con qué infraestructura. El testkit no añade JUnit al classpath
de producción del SDK base.

### SDK-06. Integración con MIEC Prototype

Agregar la dependencia y mapear gradualmente sus contratos internos al contrato
externo. Mantener inicialmente las clases de coordinación internas que tengan
responsabilidades distintas; no trasladarlas completas a la biblioteca. Seleccionar
fuentes de evidencia desde configuración confiable del servidor.

Validación: autorización, vigencia, propiedad, incertidumbre y reconciliación
conservan sus protecciones. Cambios de contrato incompatibles se identifican de
forma explícita. Ejecutar las regresiones existentes y comprobar el JAR.

### SDK-07. PetClinic como primer consumidor real

Incorporar el JAR y sustituir modelos y validaciones duplicados que correspondan al
contrato común. Conservar en PetClinic las reglas de visitas, su autenticación y su
persistencia propia. Coordinar efecto y registro idempotente en su transacción.

Validación: pruebas de contrato, clave con otra huella, solicitudes concurrentes,
rollback, pérdida de respuesta posterior al commit y reinicio en otra JVM.
Verificar que la dependencia no incorpora servicios internos de MIEC.

### SDK-08. Segundo consumidor y ajuste de abstracciones

Adaptar el experimento de reservas a la misma dependencia, conservando su dominio
y almacenamiento. Corregir únicamente abstracciones que ambos consumidores necesiten.

Validación: ejecución, consulta y evidencia funcionan sin modificar el núcleo del
SDK para cada destinatario. Distinguir la simulación local de respuesta perdida de
las pruebas de transporte real. Otra disciplina queda para un incremento posterior.

### SDK-09. Proveedor de persistencia opcional, condicionado a necesidad

Si los dos consumidores justifican reutilizar infraestructura, crear un artefacto
separado miec-recipient-jdbc. El SDK base no lo depende ni lo activa automáticamente.
Definir proveedor SQL, scripts, conexión y quién administra cada transacción.

Validación: un consumidor usa persistencia propia y otro el proveedor opcional;
ambos cumplen las mismas garantías exigidas. Probar rollback, concurrencia y
reapertura con el motor realmente soportado. H2 no acredita compatibilidad con
PostgreSQL u otros motores. Este paso puede posponerse sin bloquear el SDK base.

### SDK-10. Versión consumible y documentación

Publicar primero en Maven local y validar consumidores mediante coordenadas Maven,
sin copiar fuentes ni utilizar el JAR ejecutable de PetClinic como dependencia.
Documentar instalación, configuración, extensiones, capacidades, limitaciones y
política de compatibilidad. Publicar el testkit como artefacto independiente cuando
se distribuya entre repositorios.

Validación: construcción reproducible del SDK, consumidores compilando con el
artefacto publicado, regresiones aprobadas y revisión del contenido de los JAR.
Un starter de Spring Boot y un repositorio remoto de artefactos son incrementos
posteriores, no requisitos para esta primera versión.

## Estructura inicial propuesta

```text
miec-recipient-sdk/
  pom.xml
  docs/
    ROADMAP.md
    contract.md
    capabilities.md
    transaction-model.md
  src/main/java/com/miec/recipient/
    api/
    validation/
    spi/
  src/test/java/com/miec/recipient/
```

SDK-01 y SDK-02 están implementados; la API está en com.miec.recipient.api.
Los paquetes adicionales se crearán cuando sean necesarios. El testkit y el proveedor JDBC, si se
construyen, tendrán artefactos propios y no se empaquetarán dentro del JAR base.

## Forma de ejecución

Antes de implementar cada paso, explicar sus pruebas nuevas. Superadas las pruebas
y validaciones, registrar en consola el resultado y continuar al siguiente paso.
Agregar los archivos nuevos al repositorio correspondiente cuando esté inicializado;
no crear commits ni publicar artefactos remotos sin indicación del usuario.

Estado: SDK-01 y SDK-02 completados, con 24 pruebas aprobadas, compatibilidad
UTF8-V1 contrastada con MIEC y JAR Java 17 inspeccionado. Los pasos posteriores
permanecen pendientes. El siguiente es SDK-03: interfaces de ejecución y consulta.
Ver docs/progress.md para el alcance y los límites de las validaciones.
Actualización de validación: PetClinic ya consume los modelos del JAR mediante
Maven y conserva su propia persistencia; MIEC utiliza el SDK solo en el adaptador
experimental de test. Es una validación anticipada del contrato actual, no la
finalización de SDK-03 ni de SDK-06/SDK-07 completos. Ver docs/progress.md.

La ruta detallada del siguiente incremento, con proyectos responsables y criterios
de avance, está en [SDK-03](SDK-03.md).