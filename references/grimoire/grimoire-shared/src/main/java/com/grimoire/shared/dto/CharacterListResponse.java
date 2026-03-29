package com.grimoire.shared.dto;

import java.io.Serializable;
import java.util.List;

/**
 * Response containing list of characters for an account.
 */
public record CharacterListResponse(
        String sessionId,
        List<CharacterInfo> characters
) implements Serializable {
    public record CharacterInfo(
            Long id,
            String name,
            int level,
            String lastZone
    ) implements Serializable {
    }
}
