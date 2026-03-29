package com.grimoire.contracts.wire.dto;

import java.io.Serializable;

/**
 * Request to select a character for gameplay.
 */
public record CharacterSelectionRequest(Long characterId) implements Serializable {
}
