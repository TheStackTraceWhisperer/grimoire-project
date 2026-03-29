package com.grimoire.application.core.ecs;

/**
 * Interface for all game systems.
 *
 * <p>
 * Systems execute game logic each tick. They are scheduled in a fixed order by
 * {@link SystemScheduler}.
 * </p>
 */
@FunctionalInterface
public interface GameSystem {

    /**
     * Executes the system logic for one tick.
     *
     * @param deltaTime
     *            time elapsed since last tick in seconds
     */
    void tick(float deltaTime);
}
