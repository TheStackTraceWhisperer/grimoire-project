package com.grimoire.shared.dto;

import com.grimoire.shared.component.ComponentDTO;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Game state update containing entity component deltas.
 */
public record GameStateUpdate(
        long timestamp,
        Map<String, List<ComponentDTO>> entityUpdates
) implements Serializable {
    public GameStateUpdate {
        entityUpdates = deepImmutableCopy(entityUpdates);
    }

    @Override
    public Map<String, List<ComponentDTO>> entityUpdates() {
        return deepImmutableCopy(entityUpdates);
    }

    private static Map<String, List<ComponentDTO>> deepImmutableCopy(Map<String, List<ComponentDTO>> source) {
        return source
                .entrySet()
                .stream()
                .collect(Collectors.toUnmodifiableMap(
                        Map.Entry::getKey,
                        entry -> List.copyOf(entry.getValue())
                ));
    }
}
