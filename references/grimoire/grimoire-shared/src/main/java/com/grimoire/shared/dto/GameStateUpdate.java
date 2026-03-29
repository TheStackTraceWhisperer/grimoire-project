package com.grimoire.shared.dto;

import com.grimoire.shared.component.ComponentDTO;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Game state update containing entity component deltas.
 */
public record GameStateUpdate(
        long timestamp,
        Map<String, List<ComponentDTO>> entityUpdates
) implements Serializable {
}
