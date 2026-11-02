package com.miec.recipient.api;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Locale;

/** Snapshot inmutable del destinatario y del texto exacto; no interpreta el dominio ni persiste datos. */
public record OperationPayload(String destinationId, String mediaType, String body) {
    public static final String FINGERPRINT_FORMAT = "UTF8-V1";
    public static final int MAX_METADATA_LENGTH = 255;
    public static final int MAX_BODY_BYTES = 1_048_576;

    /** Normaliza metadatos y valida Unicode y tamaño sin alterar el cuerpo. */
    public OperationPayload {
        destinationId = ContractValues.text(destinationId, MAX_METADATA_LENGTH, "destinationId");
        mediaType = ContractValues.text(mediaType, MAX_METADATA_LENGTH, "mediaType").toLowerCase(Locale.ROOT);
        body = ContractValues.required(body, "body");
        ContractValues.unicode(body, "body");
        if (body.getBytes(StandardCharsets.UTF_8).length > MAX_BODY_BYTES) throw ContractValues.invalid("body");
    }

    /** Entrega una copia independiente de los bytes que debe utilizar el transporte. */
    public byte[] bodyBytes() { return body.getBytes(StandardCharsets.UTF_8); }

    /** Calcula SHA-256 con longitudes big endian y la representación UTF8-V1 de MIEC. */
    public String fingerprint() {
        try {
            var digest = MessageDigest.getInstance("SHA-256");
            for (String field : new String[]{FINGERPRINT_FORMAT, destinationId, mediaType, body}) {
                byte[] bytes = field.getBytes(StandardCharsets.UTF_8);
                digest.update(ByteBuffer.allocate(Integer.BYTES).putInt(bytes.length).array());
                digest.update(bytes);
            }
            return HexFormat.of().formatHex(digest.digest());
        } catch (NoSuchAlgorithmException unavailable) {
            throw new IllegalStateException("SHA-256 no está disponible", unavailable);
        }
    }
}
