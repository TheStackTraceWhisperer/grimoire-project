package com.grimoire.contracts.wire.component;

import com.grimoire.contracts.api.component.ComponentDTO;

/**
 * Position component DTO for entity location.
 */
public record PositionDTO(double x, double y) implements ComponentDTO {
}
