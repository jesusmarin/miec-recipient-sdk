# Avance del desarrollo

## SDK-01 — completado

Contrato lógico y matriz de capacidades documentados. Revisados con visitas y
reservas; simulación eléctrica solo como caso de diseño. La persistencia es una
extensión opcional y las garantías durables necesitan una implementación real.
No se redefine la planificación del proyecto ni se añaden entidades de dominio.

## SDK-02 — completado

Modelos Java y validaciones independientes del transporte, framework y persistencia.
Las huellas se calculan sobre el snapshot exacto, con la representación UTF8-V1.
Se valida coherencia de estados/referencias y prerrequisitos de capacidades sin
habilitar automáticamente capacidades ni rutas del coordinador.

Validaciones realizadas:

- Maven verify: 24 pruebas, cero fallos, errores u omisiones.
- Seis vectores fijos SHA-256 calculados con una implementación independiente;
  cubren JSON, whitespace, Unicode y cuerpo vacío.
- Comparación adicional de siete casos entre OperationPayload del JAR y la clase
  ExecutionRequest compilada de MIEC: huellas y bytes idénticos. Incluye recorte y
  conversión de metadatos. La comparación no se distribuye como dependencia del SDK.
- Límite exacto de un MiB UTF-8, copia defensiva de bytes, Unicode suplementario y
  rechazo de sustitutos sin pareja.
- JAR inspeccionado: diez clases compiladas para Java 17; jdeps informa únicamente
  java.base. No contiene JUnit, pruebas, clases de MIEC, PetClinic o drivers SQL.
- Construcción con JDK 21 y --release 17; no se ejecutó una JVM 17 adicional.

Para ejecutar las pruebas fue necesario descargar junit-platform-launcher 1.11.4,
que no estaba en la caché inicial. Después Maven verify terminó con BUILD SUCCESS.
El registro local de esa ejecución está en review-sdk-initial-development.log del
workspace de MIEC, ignorado por Git. Los informes del SDK están en target/surefire-reports.

## Pendiente

SDK-03: interfaces de ejecución y consulta y requisitos de uso de cada capacidad.
Después se desarrollarán las extensiones, pruebas de cumplimiento e integración
con consumidores siguiendo ROADMAP.md. No se modificó el código de MIEC/PetClinic
ni se incorporó el JAR en esos consumidores en esta entrega.