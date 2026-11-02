package com.miec.recipient.api;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.*;

/** Comprueba el contrato UTF8-V1 con vectores fijos calculados por una implementación independiente. */
class OperationPayloadTest {
    /** Incluye JSON con espacios, Unicode y cuerpo vacío, sin equivalencia semántica de documentos. */
    static Stream<Arguments> vectors() {
        return Stream.of(
                Arguments.of("destination", "application/json", "{}", "69b1a1d0f318a699ff3a82e37e962f74d365d66f5ea2e071931426c053daca1f"),
                Arguments.of("destination", "application/json", "{\"x\":1}", "136d83d8a1867e522711602e2978a9f00821c34217ae0fb895cb598d3616d08c"),
                Arguments.of("destination", "application/json", "{ \"x\":1}", "326579967414cff70e36bee252cf02361a0ce857b81c447b2ef82c2cea1ced84"),
                Arguments.of("simulation", "text/plain", "Voltaje: 230 V; señal Ω ⚡", "f0b51d164ab01dfbfe97dc1ca1ce076ef003601591053a6c56a517134bf0eabb"),
                Arguments.of("destination", "text/plain", "", "40f582ef91bc9972b13ca0a6cc1daae08819b499778884972fcda138e9f0eb3c"),
                Arguments.of("dest-á", "application/json", "{\"nombre\":\"niño\"}", "9d028de54e34bc34366779e7910d0a4e61562ead7c7f8e7377ee4221c3cfb854"));
    }

    /** Fija la representación exacta del protocolo; cambiar el framing o codificación rompe el vector. */
    @ParameterizedTest @MethodSource("vectors")
    void shouldMatchFingerprintVectors(String destination, String mediaType, String body, String expected) {
        assertEquals(expected, new OperationPayload(destination, mediaType, body).fingerprint());
    }

    /** La normalización depende del contrato y no del idioma del equipo consumidor. */
    @Test void shouldNormalizeMetadataWithoutChangingBody() {
        Locale previous = Locale.getDefault();
        try {
            Locale.setDefault(Locale.forLanguageTag("tr-TR"));
            var payload = new OperationPayload(" Destination ", " TEXT/PLAIN ", "  señal\n");
            assertEquals("Destination", payload.destinationId());
            assertEquals("text/plain", payload.mediaType());
            assertEquals("  señal\n", payload.body());
            assertEquals(new OperationPayload("Destination", "text/plain", "  señal\n").fingerprint(), payload.fingerprint());
        } finally { Locale.setDefault(previous); }
    }

    /** Todo metadato y cada byte del cuerpo participan de la huella. */
    @Test void shouldDistinguishDestinationMediaTypeAndBody() {
        var payload = new OperationPayload("destination", "application/json", "{}");
        assertNotEquals(payload.fingerprint(), new OperationPayload("other", "application/json", "{}").fingerprint());
        assertNotEquals(payload.fingerprint(), new OperationPayload("destination", "text/plain", "{}").fingerprint());
        assertNotEquals(payload.fingerprint(), new OperationPayload("destination", "application/json", "{} ").fingerprint());
    }

    /** Mutar el arreglo entregado al transporte no altera el snapshot ni su identidad. */
    @Test void shouldDefendAgainstByteMutation() {
        var payload = new OperationPayload("destination", "text/plain", "niño ⚡");
        String fingerprint = payload.fingerprint();
        byte[] bytes = payload.bodyBytes();
        bytes[0] = 0;
        assertArrayEquals("niño ⚡".getBytes(StandardCharsets.UTF_8), payload.bodyBytes());
        assertEquals(fingerprint, payload.fingerprint());
    }

    /** El límite se aplica a bytes UTF-8, aceptando exactamente un MiB y rechazando el siguiente byte. */
    @Test void shouldEnforceUtf8ByteLimit() {
        String full = "é".repeat(OperationPayload.MAX_BODY_BYTES / 2);
        assertEquals(OperationPayload.MAX_BODY_BYTES, new OperationPayload("d", "text/plain", full).bodyBytes().length);
        assertEquals(ProtocolErrorCode.INVALID_REQUEST, assertThrows(ProtocolException.class,
                () -> new OperationPayload("d", "text/plain", full + "a")).code());
    }

    /** Metadatos vacíos, ausentes o extensos y cuerpo ausente se rechazan con mensajes sin datos recibidos. */
    @Test void shouldRejectInvalidFields() {
        assertThrows(ProtocolException.class, () -> new OperationPayload(null, "text/plain", ""));
        assertThrows(ProtocolException.class, () -> new OperationPayload(" ", "text/plain", ""));
        assertThrows(ProtocolException.class, () -> new OperationPayload("x".repeat(256), "text/plain", ""));
        assertThrows(ProtocolException.class, () -> new OperationPayload("d", null, ""));
        assertThrows(ProtocolException.class, () -> new OperationPayload("d", " ", ""));
        assertThrows(ProtocolException.class, () -> new OperationPayload("d", "x".repeat(256), ""));
        assertThrows(ProtocolException.class, () -> new OperationPayload("d", "text/plain", null));
        assertDoesNotThrow(() -> new OperationPayload("x".repeat(255), "x".repeat(255), ""));
    }

    /** Los sustitutos sin pareja no pueden colisionar mediante el carácter de reemplazo del encoder. */
    @Test void shouldRejectMalformedUnicodeAndAcceptSupplementaryCharacters() {
        for (String malformed : new String[]{String.valueOf((char) 0xd800), String.valueOf((char) 0xdc00),
                String.valueOf((char) 0xd800) + "a"}) {
            assertThrows(ProtocolException.class, () -> new OperationPayload("d", "text/plain", malformed));
            assertThrows(ProtocolException.class, () -> new OperationPayload(malformed, "text/plain", ""));
        }
        assertArrayEquals(new byte[]{(byte) 0xf0, (byte) 0x9f, (byte) 0x98, (byte) 0x80},
                new OperationPayload("d", "text/plain", "😀").bodyBytes());
    }
}
