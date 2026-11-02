package com.miec.recipient.api;

import java.util.Objects;

/** Rechazo del contrato con código estable; los mensajes nunca deben incluir secretos. */
public final class ProtocolException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    private final ProtocolErrorCode code;

    /** Conserva un código de protocolo y un mensaje seguro, independiente de HTTP. */
    public ProtocolException(ProtocolErrorCode code, String message) {
        super(Objects.requireNonNull(message, "message"));
        this.code = Objects.requireNonNull(code, "code");
    }

    /** Devuelve la causa semántica sin interpretar el estado de la operación. */
    public ProtocolErrorCode code() { return code; }
}
