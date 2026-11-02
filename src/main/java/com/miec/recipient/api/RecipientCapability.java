package com.miec.recipient.api;

/** Compromisos que una implementación configurada debe demostrar mediante pruebas. */
public enum RecipientCapability {
    /** Consultar resultados sin ejecutar la operación. */
    LOOKUP,
    /** Conservar clave y resultado y rechazar conflictos mediante almacenamiento durable. */
    DURABLE_DEDUPLICATION,
    /** Confirmar o revertir juntos clave, efecto y resultado. */
    ATOMIC_EFFECT_RESULT,
    /** Consultar el resultado conservado después de reiniciar el destinatario. */
    RECOVERY_AFTER_RESTART
}
