package com.grimoire.contracts.wire.component;

import com.grimoire.contracts.api.component.ComponentDTO;

/**
 * Portal component DTO for portal dimensions.
 */
public record PortalDTO(double width, double height) implements ComponentDTO {
}
