package com.miec.recipient.api;

/** Validaciones internas: no expone el contenido recibido en mensajes de error. */
final class ContractValues {
    /** Evita instancias de la utilidad interna. */
    private ContractValues() { }

    /** Exige presencia sin introducir una dependencia de otro proyecto. */
    static <T> T required(T value, String field) {
        if (value == null) throw invalid(field);
        return value;
    }

    /** Aplica el mismo recorte y límite de metadatos que UTF8-V1 de MIEC. */
    static String text(String value, int max, String field) {
        String normalized = required(value, field).trim();
        if (normalized.isEmpty() || normalized.length() > max) throw invalid(field);
        unicode(normalized, field);
        return normalized;
    }

    /** Rechaza sustitutos sin pareja antes de convertir un String a bytes UTF-8. */
    static void unicode(String value, String field) {
        for (int i = 0; i < value.length(); i++) {
            char current = value.charAt(i);
            if (Character.isHighSurrogate(current)) {
                if (i + 1 >= value.length() || !Character.isLowSurrogate(value.charAt(++i))) throw invalid(field);
            } else if (Character.isLowSurrogate(current)) {
                throw invalid(field);
            }
        }
    }

    /** Exige una huella hexadecimal canónica; no admite una representación ambigua. */
    static String fingerprint(String value) {
        if (value == null || !value.matches("[0-9a-f]{64}")) throw invalid("fingerprint");
        return value;
    }

    /** Genera un error seguro con el nombre fijo del campo. */
    static ProtocolException invalid(String field) {
        return new ProtocolException(ProtocolErrorCode.INVALID_REQUEST, "Campo inválido: " + field);
    }
}
