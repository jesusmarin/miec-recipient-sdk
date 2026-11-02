package com.miec.recipient.api;

/** Códigos independientes del transporte; un error no prueba ausencia de efecto. */
public enum ProtocolErrorCode {
    INVALID_REQUEST,
    FINGERPRINT_MISMATCH,
    OPERATION_CONFLICT,
    UNSUPPORTED_CAPABILITY,
    UNAUTHORIZED,
    TEMPORARILY_UNAVAILABLE
}
