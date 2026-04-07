package com.grimoire.application.core.ecs;

/**
 * Interface for all game systems.
 *
 * <p>
 * Systems execute game logic each tick. They are scheduled in a fixed order by
 * {@link SystemScheduler}. Time is expressed as a monotonically increasing
 * integer tick counter to avoid floating-point drift over long uptimes.
 * </p>
 */
@FunctionalInterface
public interface GameSystem {

    /**
     * Executes the system logic for one tick.
     *
     * @param currentTick
     *            the current game tick (monotonically increasing)
     */
    void tick(long currentTick);
}
