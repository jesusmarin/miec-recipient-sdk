package com.miec.recipient.api;

import java.util.UUID;

/** Observación ligada a clave, destino y huella; no acredita autenticación por sí misma. */
public record OperationResult(UUID operationKey, String destinationId, String fingerprint,
                              OperationStatus status, String resultReference) {
    public static final int MAX_REFERENCE_LENGTH = 500;

    /** Exige identidad y coherencia entre estado y referencia opaca. */
    public OperationResult {
        operationKey = ContractValues.required(operationKey, "operationKey");
        destinationId = ContractValues.text(destinationId, OperationPayload.MAX_METADATA_LENGTH, "destinationId");
        fingerprint = ContractValues.fingerprint(fingerprint);
        status = ContractValues.required(status, "status");
        if (status == OperationStatus.APPLIED) {
            resultReference = ContractValues.text(resultReference, MAX_REFERENCE_LENGTH, "resultReference");
        } else if (resultReference != null) {
            throw ContractValues.invalid("resultReference");
        }
    }

    /** Comprueba el vínculo con la solicitud esperada; no verifica la fuente ni autoriza reintentos. */
    public boolean matches(OperationRequest expected) {
        return expected != null && operationKey.equals(expected.operationKey())
                && destinationId.equals(expected.payload().destinationId())
                && fingerprint.equals(expected.fingerprint());
    }
}
