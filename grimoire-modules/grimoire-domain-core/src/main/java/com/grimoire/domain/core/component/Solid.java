package com.grimoire.domain.core.component;

/**
 * Marker component indicating an entity is solid and blocks movement.
 *
 * <p>
 * Entities with this component cannot be passed through by other entities. Used
 * for walls, NPCs, and other obstacles.
 * </p>
 */
public record Solid() implements Component {
}
