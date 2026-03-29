package com.grimoire.contracts.wire.dto;

import java.io.Serializable;
import java.util.List;

/**
 * Response containing list of characters for an account.
 */
public record CharacterListResponse(
        String sessionId,
        List<CharacterInfo> characters) implements Serializable {

    public CharacterListResponse {
        characters = characters == null ? List.of() : List.copyOf(characters);
    }

    /**
     * Summary information about a single character.
     */
    public record CharacterInfo(
            Long id,
            String name,
            int level,
            String lastZone) implements Serializable {
    }
}
