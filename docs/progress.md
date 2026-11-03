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
con consumidores siguiendo ROADMAP.md. En SDK-01/SDK-02 no se incorporó el JAR
en los consumidores; la validación posterior se describe a continuación.
## Validación anticipada de modelos con PetClinic

El JAR se instaló en Maven local y PetClinic lo consume por coordenadas Maven.
El destinatario usa OperationPayload, OperationRequest y OperationResult; mantiene
la visita, su validación de dominio y el ledger en su propia transacción.
La decodificación UTF-8 es estricta antes de calcular la huella del cuerpo exacto.

El adaptador experimental de MIEC valida las respuestas mediante el mismo JAR y
comprueba clave, destino y huella. Su dependencia tiene scope test y no se incluye
en el JAR productivo de MIEC. No se completan por adelantado las interfaces SDK-03
ni se da por terminada toda la ruta del SDK.

Pruebas aprobadas hasta esta adaptación: SDK 24, contrato HTTP PetClinic 6,
adaptador MIEC 4, regresión MIEC 688 y PetClinic sin contenedores 83. Las clases
MySqlIntegrationTests y PostgresIntegrationTests se excluyeron por requerir Docker.
La prueba entre procesos y su alcance de cierre se documentarán al finalizar en
docs/IR-009-C09-sdk-validation.md del repositorio miec-prototype.

Resultado final de integración: las 5 pruebas explícitas PetClinic pasaron con
el JAR del SDK (4 de contrato y una de respuesta perdida con reinicio en otra JVM).
Se corrigió una liberación transitoria del archivo en Windows en el soporte de
pruebas de MIEC: espera acotada exclusivamente para H2 90020 después de salir la JVM,
sin reintentar conteos ni otros errores SQL. El punto experimental C09 queda
validado; SDK-03 y siguientes y el resto de IR-009 permanecen pendientes.

Se verificó que el SDK empaquetado en PetClinic tiene el mismo SHA-256 que el JAR
construido, y que producción de MIEC excluye SDK y adaptadores experimentales.
Registro final: review-sdk-petclinic-process-final.log en el workspace de MIEC.

## SDK-03, paso 1 — completado

Contrato de comportamiento y matriz de escenarios definidos en SDK-03-contract.md
y SDK-03-validation-cases.md. Ejecución recibe el snapshot conocido; consulta usa
identidad y huella esperada opcional, sin cuerpo ni huella fabricada. Se conserva
OperationResult y se especifica una envoltura futura de consulta para ausencia e
incertidumbre. Los perfiles y errores tienen reglas explícitas.

Esta es una entrega de especificación. No se implementaron aún las interfaces,
modelos nuevos ni guardas de los pasos 2 y 3. Los escenarios futuros no se reportan
como pruebas aprobadas. El avance se detiene antes del paso 2 por instrucción del
usuario. PetClinic y MIEC no se modifican en este paso.
Validación del paso 1: Maven validate y Maven verify terminaron con BUILD SUCCESS;
24 pruebas existentes aprobadas, cero fallos, errores u omisiones. Son regresiones
de los modelos actuales, no pruebas de las interfaces futuras. Registro local:
review-sdk03-step1.log en el workspace de MIEC. Proceso detenido antes del paso 2.
