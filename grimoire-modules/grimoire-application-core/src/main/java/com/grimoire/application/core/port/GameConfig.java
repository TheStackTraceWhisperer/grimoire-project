package com.grimoire.application.core.port;

/**
 * Configuration port for tunable game parameters.
 *
 * <p>
 * Infrastructure adapters provide the implementation, typically backed by
 * external configuration (e.g., Micronaut {@code @ConfigurationProperties}).
 * All methods have sensible defaults so implementations can selectively
 * override.
 * </p>
 */
public interface GameConfig {

    /**
     * Maximum attack range in world units.
     *
     * @return the attack range (default 50.0)
     */
    default double attackRange() {
        return 50.0;
    }

    /**
     * Player movement speed in units per second.
     *
     * @return the player speed (default 5.0)
     */
    default double playerSpeed() {
        return 5.0;
    }

    /**
     * Spatial grid cell size in world units.
     *
     * @return the cell size (default 64)
     */
    default int spatialGridCellSize() {
        return 64;
    }

    /**
     * NPC aggro detection range in world units.
     *
     * @return the aggro range (default 100.0)
     */
    default double npcAggroRange() {
        return 100.0;
    }

    /**
     * NPC movement speed in units per second.
     *
     * @return the NPC speed (default 3.0)
     */
    default double npcSpeed() {
        return 3.0;
    }

    /**
     * Number of ticks between consecutive attacks.
     *
     * @return the cooldown in ticks (default 20)
     */
    default int attackCooldownTicks() {
        return 20;
    }

    /**
     * Default leash radius for NPCs in world units.
     *
     * @return the leash radius (default 200.0)
     */
    default double npcLeashRadius() {
        return 200.0;
    }

    /**
     * Number of ticks an entity cannot re-enter a portal after a zone change.
     *
     * @return the portal cooldown in ticks (default 60)
     */
    default int portalCooldownTicks() {
        return 60;
    }
}
