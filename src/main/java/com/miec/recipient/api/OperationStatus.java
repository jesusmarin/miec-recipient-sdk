package com.miec.recipient.api;

/** Estado observado del destinatario, diferente del estado de un intento interno de MIEC. */
public enum OperationStatus {
    /** Efecto confirmado y ligado a la identidad de la operación. */
    APPLIED,
    /** Ausencia visible de resultado; no descarta una ejecución en vuelo. */
    NOT_FOUND,
    /** Resultado incierto; no autoriza por sí solo un nuevo envío. */
    UNKNOWN
}
