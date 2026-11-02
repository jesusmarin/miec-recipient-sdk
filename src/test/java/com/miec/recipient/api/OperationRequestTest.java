package com.miec.recipient.api;

import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

/** Prueba identidad estable y rechazo de huellas que no corresponden al contenido. */
class OperationRequestTest {
    /** Crear de nuevo la solicitud conserva clave, snapshot y huella, sin reservar recursos externos. */
    @Test void shouldPreserveIdentityAndValidateDeclaredFingerprint() {
        UUID key = UUID.randomUUID();
        var payload = new OperationPayload("d", "application/json", "{}");
        var request = new OperationRequest(key, payload);
        assertEquals(request, new OperationRequest(key, payload));
        assertEquals(payload.fingerprint(), request.fingerprint());
        assertDoesNotThrow(() -> request.requireFingerprint(payload.fingerprint()));
    }

    /** Una huella ajena, no canónica o ausente genera un error explícito sin incluirla en el mensaje. */
    @Test void shouldRejectMismatchedFingerprint() {
        var request = new OperationRequest(UUID.randomUUID(), new OperationPayload("d", "text/plain", "secret-body"));
        for (String supplied : new String[]{null, "", "0".repeat(64), request.fingerprint().toUpperCase(java.util.Locale.ROOT)}) {
            var failure = assertThrows(ProtocolException.class, () -> request.requireFingerprint(supplied));
            assertEquals(ProtocolErrorCode.FINGERPRINT_MISMATCH, failure.code());
            assertFalse(failure.getMessage().contains("secret-body"));
        }
    }

    /** Un modelo incompleto no puede representar una ejecución válida. */
    @Test void shouldRequireIdentityAndPayload() {
        assertThrows(ProtocolException.class, () -> new OperationRequest(null, new OperationPayload("d", "text/plain", "")));
        assertThrows(ProtocolException.class, () -> new OperationRequest(UUID.randomUUID(), null));
    }
}
