package com.grimoire.ecs;

/**
 * Interface for all game systems.
 */
@FunctionalInterface
public interface GameSystem {
    
    /**
     * Executes the system logic for one tick.
     * @param deltaTime time elapsed since last tick in seconds
     */
    void tick(float deltaTime);
}
