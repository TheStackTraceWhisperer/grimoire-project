package com.grimoire.server.ecs;

/**
 * Interface for all game systems.
 */
public interface GameSystem {
    
    /**
     * Executes the system logic for one tick.
     * @param deltaTime time elapsed since last tick in seconds
     */
    void tick(float deltaTime);
}
