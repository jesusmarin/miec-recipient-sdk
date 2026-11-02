package com.miec.recipient.api;

import org.junit.jupiter.api.Test;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

/** Prueba capacidades opcionales, declaración inmutable y prerrequisitos explícitos. */
class RecipientCapabilitiesTest {
    /** Usar los modelos no fuerza consulta ni una base de datos. */
    @Test void shouldAllowNoCapabilitiesAndLookupOnly() {
        assertTrue(RecipientCapabilities.none().capabilities().isEmpty());
        var lookup = new RecipientCapabilities(Set.of(RecipientCapability.LOOKUP));
        assertTrue(lookup.supports(RecipientCapability.LOOKUP));
        assertFalse(lookup.supports(RecipientCapability.DURABLE_DEDUPLICATION));
    }

    /** Cambiar la colección original o intentar editar el accessor no cambia los compromisos capturados. */
    @Test void shouldCopyAndProtectConfiguredCapabilities() {
        var configured = EnumSet.of(RecipientCapability.LOOKUP);
        var captured = new RecipientCapabilities(configured);
        configured.add(RecipientCapability.DURABLE_DEDUPLICATION);
        assertFalse(captured.supports(RecipientCapability.DURABLE_DEDUPLICATION));
        assertThrows(UnsupportedOperationException.class, () -> captured.capabilities().add(RecipientCapability.DURABLE_DEDUPLICATION));
    }

    /** Recuperación y atomicidad requieren respaldo durable, sin confundirse entre sí. */
    @Test void shouldValidatePrerequisitesWithoutInventingCapabilities() {
        assertThrows(ProtocolException.class, () -> new RecipientCapabilities(Set.of(RecipientCapability.ATOMIC_EFFECT_RESULT)));
        assertThrows(ProtocolException.class, () -> new RecipientCapabilities(Set.of(RecipientCapability.RECOVERY_AFTER_RESTART)));
        assertThrows(ProtocolException.class, () -> new RecipientCapabilities(Set.of(RecipientCapability.RECOVERY_AFTER_RESTART, RecipientCapability.DURABLE_DEDUPLICATION)));
        var recovery = new RecipientCapabilities(Set.of(RecipientCapability.RECOVERY_AFTER_RESTART,
                RecipientCapability.DURABLE_DEDUPLICATION, RecipientCapability.LOOKUP));
        assertFalse(recovery.supports(RecipientCapability.ATOMIC_EFFECT_RESULT));
        assertDoesNotThrow(() -> new RecipientCapabilities(EnumSet.allOf(RecipientCapability.class)));
    }

    /** Una declaración ausente o con entradas nulas no equivale a un perfil explícito sin capacidades. */
    @Test void shouldRejectNullConfiguration() {
        assertThrows(ProtocolException.class, () -> new RecipientCapabilities(null));
        var invalid = new HashSet<RecipientCapability>();
        invalid.add(null);
        assertThrows(ProtocolException.class, () -> new RecipientCapabilities(invalid));
        assertThrows(ProtocolException.class, () -> RecipientCapabilities.none().supports(null));
    }
}
