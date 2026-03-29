package com.grimoire.contracts.wire.dto;

import com.grimoire.contracts.api.component.ComponentDTO;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Game state update containing entity component deltas.
 */
public record GameStateUpdate(
        long timestamp,
        Map<String, List<ComponentDTO>> entityUpdates) implements Serializable {

    public GameStateUpdate {
        entityUpdates = entityUpdates == null ? Map.of() : Map.copyOf(entityUpdates);
    }
}
