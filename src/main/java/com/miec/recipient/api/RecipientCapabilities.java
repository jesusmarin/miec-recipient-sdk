package com.miec.recipient.api;

import java.util.Set;

/** Declaración inmutable de servidor; valida coherencia, no certifica el comportamiento externo. */
public record RecipientCapabilities(Set<RecipientCapability> capabilities) {
    /** Rechaza combinaciones que prometen recuperación o atomicidad sin sus prerrequisitos. */
    public RecipientCapabilities {
        ContractValues.required(capabilities, "capabilities");
        if (capabilities.stream().anyMatch(value -> value == null)) throw ContractValues.invalid("capabilities");
        capabilities = Set.copyOf(capabilities);
        boolean durable = capabilities.contains(RecipientCapability.DURABLE_DEDUPLICATION);
        if (capabilities.contains(RecipientCapability.ATOMIC_EFFECT_RESULT) && !durable) {
            throw ContractValues.invalid("capabilities");
        }
        if (capabilities.contains(RecipientCapability.RECOVERY_AFTER_RESTART)
                && (!durable || !capabilities.contains(RecipientCapability.LOOKUP))) {
            throw ContractValues.invalid("capabilities");
        }
    }

    /** Representa un consumidor que utiliza modelos sin declarar persistencia ni consulta. */
    public static RecipientCapabilities none() { return new RecipientCapabilities(Set.of()); }

    /** Consulta un compromiso declarado; no lo convierte en prueba ni habilita rutas de MIEC. */
    public boolean supports(RecipientCapability capability) {
        return capabilities.contains(ContractValues.required(capability, "capability"));
    }
}
