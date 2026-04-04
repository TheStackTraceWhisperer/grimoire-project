package com.grimoire.application.core.port;

/**
 * Outbound port for game events that require infrastructure actions.
 *
 * <p>
 * Infrastructure adapters (e.g., network layer) implement this interface to
 * react to significant game events such as entity despawns and zone changes.
 * </p>
 */
public interface GameEventPort {

    /**
     * Notifies that an entity has been despawned (e.g., killed).
     *
     * @param entityId
     *            the despawned entity's ID
     * @param zoneId
     *            the zone in which the despawn occurred
     */
    void onEntityDespawn(int entityId, String zoneId);

    /**
     * Notifies that an entity has changed zones.
     *
     * @param entityId
     *            the entity that changed zones
     * @param newZoneId
     *            the zone the entity moved to
     * @param x
     *            the new X position in the target zone
     * @param y
     *            the new Y position in the target zone
     */
    void onZoneChange(int entityId, String newZoneId, double x, double y);
}
