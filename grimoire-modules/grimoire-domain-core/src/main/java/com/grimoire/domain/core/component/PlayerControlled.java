package com.grimoire.domain.core.component;

/**
 * Component indicating an entity is controlled by a human player.
 *
 * <p>
 * The {@code sessionId} links the entity to a {@code Session} managed by the
 * session module.
 * </p>
 */
public class PlayerControlled implements Component {

    /**
     * The session identifier for this player's connection.
     */
    public String sessionId;

    /**
     * No-arg constructor for array pre-allocation.
     */
    public PlayerControlled() {
        // default values
    }

    /**
     * Creates a player-controlled marker.
     *
     * @param sessionId
     *            the session identifier
     */
    public PlayerControlled(String sessionId) {
        this.sessionId = sessionId;
    }

    /**
     * Zero-allocation update.
     *
     * @param newSessionId
     *            the new session ID
     */
    public void update(String newSessionId) {
        this.sessionId = newSessionId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PlayerControlled p)) {
            return false;
        }
        return java.util.Objects.equals(sessionId, p.sessionId);
    }

    @Override
    public int hashCode() {
        return sessionId != null ? sessionId.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "PlayerControlled[sessionId=" + sessionId + "]";
    }
}
