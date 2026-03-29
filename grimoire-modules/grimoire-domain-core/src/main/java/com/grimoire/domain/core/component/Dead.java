package com.grimoire.domain.core.component;

/**
 * Dead marker component for entities that have been killed.
 *
 * <p>
 * Entities with this component should be despawned on the next sync and then
 * removed from the ECS world.
 * </p>
 *
 * @param killerId
 *            the entity ID of the killer, or {@code null} for environmental
 *            deaths
 */
public record Dead(String killerId) implements Component {
}
