package com.grimoire.contracts.wire.dto;

import java.io.Serializable;

/**
 * Response indicating successful character selection and game entry.
 */
public record CharacterSelectionSuccess(
        String entityId,
        String characterName,
        int level,
        String zone,
        double x,
        double y) implements Serializable {
}
