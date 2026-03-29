package com.grimoire.contracts.wire.component;

import com.grimoire.contracts.api.component.ComponentDTO;

/**
 * Renderable component DTO for visual representation.
 *
 * @param name
 *            display name of the entity
 * @param visualId
 *            visual asset identifier (e.g. "visual-player",
 *            "visual-monster-rat")
 */
public record RenderableDTO(String name, String visualId) implements ComponentDTO {
}
