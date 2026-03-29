package com.grimoire.contracts.wire.dto;

import com.grimoire.contracts.api.component.ComponentDTO;

import java.io.Serializable;
import java.util.List;

/**
 * Entity spawn notification with all components.
 */
public record EntitySpawn(String entityId, List<ComponentDTO> allComponents) implements Serializable {

    public EntitySpawn {
        allComponents = allComponents == null ? List.of() : List.copyOf(allComponents);
    }
}
