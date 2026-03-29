package com.grimoire.contracts.wire.component;

import com.grimoire.contracts.api.component.ComponentDTO;

/**
 * Stats component DTO for health information.
 */
public record StatsDTO(int currentHp, int maxHp) implements ComponentDTO {
}
