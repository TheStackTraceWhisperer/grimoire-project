package com.grimoire.domain.core.component;

/**
 * Component indicating an entity is controlled by a human player.
 *
 * <p>
 * This separates the concept of "player-controlled" from infrastructure
 * concerns such as network connections. Systems that need to distinguish player
 * entities from NPCs should check for this component.
 * </p>
 *
 * <p>
 * The {@code sessionId} links the entity to a {@code Session} managed by the
 * session module. The infrastructure layer (e.g., Netty) maintains its own
 * mapping from session ID to transport channel — the domain never sees a
 * {@code Channel} reference.
 * </p>
 *
 * @param sessionId
 *            the session identifier for this player's connection
 */
public record PlayerControlled(String sessionId) implements Component {
}
