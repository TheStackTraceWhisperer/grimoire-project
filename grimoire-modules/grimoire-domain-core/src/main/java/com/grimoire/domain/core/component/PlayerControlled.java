package com.grimoire.domain.core.component;

/**
 * Marker component indicating an entity is controlled by a human player.
 *
 * <p>
 * This separates the concept of "player-controlled" from infrastructure
 * concerns such as network connections. Systems that need to distinguish player
 * entities from NPCs should check for this component.
 * </p>
 */
public record PlayerControlled() implements Component {
}
