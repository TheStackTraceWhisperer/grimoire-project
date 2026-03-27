package com.grimoire.shared.component;

/**
 * Renderable component DTO for visual representation.
 * visualId examples: "visual-player", "visual-monster-rat", "visual-portal"
 */
public record RenderableDTO(String name, String visualId) implements ComponentDTO {
}
