package com.grimoire.shared.dto;

import com.grimoire.shared.component.ComponentDTO;

import java.io.Serializable;
import java.util.List;

/**
 * Entity spawn notification with all components.
 */
public record EntitySpawn(String entityId, List<ComponentDTO> allComponents) implements Serializable {
    public EntitySpawn {
        allComponents = List.copyOf(allComponents);
    }
}
