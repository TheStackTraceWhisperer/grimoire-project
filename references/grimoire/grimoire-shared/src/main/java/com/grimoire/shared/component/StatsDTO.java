package com.grimoire.shared.component;

/**
 * Stats component DTO for health information.
 */
public record StatsDTO(int currentHp, int maxHp) implements ComponentDTO {
}
