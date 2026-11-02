package com.miec.recipient.api;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

/** Valida coherencia del resultado y vínculo completo con la solicitud esperada. */
class OperationResultTest {
    /** Devuelve una operación del dominio de referencia sin introducirlo en la API. */
    private OperationRequest request() {
        return new OperationRequest(UUID.randomUUID(), new OperationPayload("d", "application/json", "{}"));
    }

    /** APPLIED exige referencia y conserva la coincidencia de clave, destino y huella. */
    @Test void shouldAcceptAppliedResultWithBoundIdentity() {
        var request = request();
        var result = new OperationResult(request.operationKey(), " d ", request.fingerprint(), OperationStatus.APPLIED, " result:1 ");
        assertEquals("result:1", result.resultReference());
        assertTrue(result.matches(request));
        assertFalse(result.matches(null));
        assertFalse(result.matches(new OperationRequest(UUID.randomUUID(), request.payload())));
        assertFalse(result.matches(new OperationRequest(request.operationKey(), new OperationPayload("other", "application/json", "{}"))));
        assertFalse(result.matches(new OperationRequest(request.operationKey(), new OperationPayload("d", "application/json", "{} "))));
    }

    /** Los estados inciertos no arrastran una referencia que pueda confundirse con un efecto confirmado. */
    @ParameterizedTest @EnumSource(value=OperationStatus.class, names={"NOT_FOUND", "UNKNOWN"})
    void shouldRejectReferencesOnUnconfirmedResults(OperationStatus status) {
        var request = request();
        var result = new OperationResult(request.operationKey(), "d", request.fingerprint(), status, null);
        assertTrue(result.matches(request));
        assertNull(result.resultReference());
        assertThrows(ProtocolException.class, () -> new OperationResult(request.operationKey(), "d", request.fingerprint(), status, "ref"));
    }

    /** La referencia aplicada es obligatoria y tiene el mismo límite de MIEC. */
    @Test void shouldValidateAppliedReference() {
        var request = request();
        for (String invalid : new String[]{null, "", " ", "x".repeat(501)}) {
            assertThrows(ProtocolException.class, () -> new OperationResult(request.operationKey(), "d", request.fingerprint(), OperationStatus.APPLIED, invalid));
        }
        assertDoesNotThrow(() -> new OperationResult(request.operationKey(), "d", request.fingerprint(), OperationStatus.APPLIED, "x".repeat(500)));
    }

    /** Sin identidad y huella canónica no se puede aceptar una observación externa. */
    @Test void shouldRejectInvalidResultIdentity() {
        var request = request();
        assertThrows(ProtocolException.class, () -> new OperationResult(null, "d", request.fingerprint(), OperationStatus.UNKNOWN, null));
        assertThrows(ProtocolException.class, () -> new OperationResult(request.operationKey(), " ", request.fingerprint(), OperationStatus.UNKNOWN, null));
        assertThrows(ProtocolException.class, () -> new OperationResult(request.operationKey(), "d", null, OperationStatus.UNKNOWN, null));
        assertThrows(ProtocolException.class, () -> new OperationResult(request.operationKey(), "d", "x".repeat(64), OperationStatus.UNKNOWN, null));
        assertThrows(ProtocolException.class, () -> new OperationResult(request.operationKey(), "d", request.fingerprint().toUpperCase(java.util.Locale.ROOT), OperationStatus.UNKNOWN, null));
        assertThrows(ProtocolException.class, () -> new OperationResult(request.operationKey(), "d", request.fingerprint(), null, null));
    }
}
