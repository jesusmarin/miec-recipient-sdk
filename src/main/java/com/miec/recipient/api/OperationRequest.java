package com.miec.recipient.api;

import java.util.UUID;

/** Identidad lógica estable y solicitud exacta; no incluye permisos ni entidades internas de MIEC. */
public record OperationRequest(UUID operationKey, OperationPayload payload) {
    /** Exige clave y snapshot; la huella se deriva siempre del contenido capturado. */
    public OperationRequest {
        operationKey = ContractValues.required(operationKey, "operationKey");
        payload = ContractValues.required(payload, "payload");
    }

    /** Obtiene la huella local, sin confiar en un atributo mutable del llamador. */
    public String fingerprint() { return payload.fingerprint(); }

    /** Valida una huella recibida antes de que el consumidor ejecute efectos. */
    public void requireFingerprint(String declaredFingerprint) {
        if (!fingerprint().equals(declaredFingerprint)) {
            throw new ProtocolException(ProtocolErrorCode.FINGERPRINT_MISMATCH, "La huella no corresponde a la solicitud");
        }
    }
}
