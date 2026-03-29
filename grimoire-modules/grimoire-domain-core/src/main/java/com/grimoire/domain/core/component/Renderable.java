package com.grimoire.domain.core.component;

/**
 * Renderable component for visual representation of an entity.
 *
 * @param name
 *            the display name
 * @param visualId
 *            the identifier for the visual asset (sprite, model, etc.)
 */
public record Renderable(String name, String visualId) implements Component {
}
