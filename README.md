# MIEC Recipient SDK

Biblioteca Java independiente para implementar contratos de destinatarios de MIEC.
Artefacto: `com.miec:miec-recipient-sdk:0.1.0-SNAPSHOT`. Requiere Java 17 o posterior.

## Estado

SDK-01 y SDK-02 están implementados: contrato, capacidades y modelos inmutables con
validaciones. No hay implementación de ejecución, consulta, persistencia, servidor
HTTP ni autenticación. Esas responsabilidades siguen en la aplicación consumidora.
PetClinic ya consume el JAR en su integración experimental; MIEC lo utiliza solo
en el adaptador de test. Esto valida los modelos, sin completar todavía SDK-03.
El siguiente incremento es SDK-03, interfaces de ejecución y consulta.

## Construcción

Desde la raíz del SDK, con Maven y JDK 17 o posterior:

```powershell
mvn clean verify
```

El proyecto aún no incluye Maven Wrapper propio. El JAR se genera en
`target/miec-recipient-sdk-0.1.0-SNAPSHOT.jar`. JUnit se utiliza solo en pruebas;
la API productiva depende únicamente del JDK. La validación actual se ejecutó en
JDK 21 compilando con `--release 17`; no se afirma una ejecución adicional en JDK 17.

## Modelos disponibles

| Clase | Propósito |
|---|---|
| OperationPayload | Destino, tipo de contenido, cuerpo exacto y huella UTF8-V1. |
| OperationRequest | Clave estable y payload inmutable; verifica una huella declarada. |
| OperationStatus | APPLIED, NOT_FOUND y UNKNOWN; no son estados de intentos internos de MIEC. |
| OperationResult | Resultado vinculado a clave, destino y huella con referencia opaca. |
| RecipientCapability | Capacidades que una implementación debe demostrar. |
| RecipientCapabilities | Declaración inmutable y coherente, sin activación automática. |
| ProtocolErrorCode / ProtocolException | Errores del contrato sin dependencia de HTTP. |

Ejemplo de construcción de datos, sin ejecutar efectos:

```java
import com.miec.recipient.api.OperationPayload;
import com.miec.recipient.api.OperationRequest;
import java.util.UUID;

var payload = new OperationPayload("my-destination", "application/json", "{\"units\":3}");
var request = new OperationRequest(UUID.randomUUID(), payload);
byte[] exactBody = payload.bodyBytes();
String fingerprint = request.fingerprint();
```

La clave la asigna normalmente el coordinador; el consumidor no debe generar otra
al recibir un reintento. Antes de ejecutar, debe comparar la huella recibida mediante
`request.requireFingerprint(receivedFingerprint)`. Ese método valida contenido, no
autentica al llamador ni confirma que el efecto no haya ocurrido.

`RecipientCapabilities.none()` permite utilizar los modelos sin instalar una base
de datos. Declarar deduplicación durable o recuperación exige una implementación
que cumpla y demuestre esas garantías; el SDK no las crea al construir un objeto.

## Documentación

- [Ruta de desarrollo](docs/ROADMAP.md)
- [Contrato](docs/contract.md)
- [Capacidades](docs/capabilities.md)
- [Avance y validaciones](docs/progress.md)

## Consumidor experimental PetClinic

Antes de construir PetClinic o las pruebas de MIEC, instalar el SDK en el mismo
repositorio Maven local que utilicen los consumidores. Desde la raíz del SDK:

```powershell
mvn clean install
```

PetClinic declara com.miec:miec-recipient-sdk:0.1.0-SNAPSHOT como dependencia de
compilación; MIEC la declara con scope test. No se copian fuentes del SDK ni se
usa el JAR ejecutable de PetClinic como biblioteca. Esta integración utiliza los
modelos y validaciones actuales; las interfaces SDK-03 siguen pendientes.