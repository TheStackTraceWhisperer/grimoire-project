package com.grimoire.application.core.ecs;

import java.util.List;
import java.util.Objects;

/**
 * Schedules and executes all game systems in a fixed order.
 *
 * <p>
 * Systems are ticked sequentially in the order provided at construction time.
 * After all systems have been ticked, the world tick counter is incremented.
 * </p>
 *
 * <p>
 * The ordering of systems is determined by the assembly layer
 * (application/infrastructure modules) — this class does not impose any
 * ordering policy.
 * </p>
 *
 * <p>
 * <strong>Thread-safety:</strong> Must be called from the single-threaded game
 * loop only. Register as a singleton at the assembly layer.
 * </p>
 */
public class SystemScheduler {

    /** The ECS world whose tick counter is incremented each tick. */
    private final EcsWorld ecsWorld;

    /** Immutable ordered list of systems to execute each tick. */
    private final List<GameSystem> systems;

    /**
     * Creates a scheduler with the given world and system list.
     *
     * @param ecsWorld
     *            the ECS world whose tick counter is incremented each tick
     * @param systems
     *            the ordered list of systems to execute each tick
     */
    public SystemScheduler(EcsWorld ecsWorld, List<GameSystem> systems) {
        this.ecsWorld = Objects.requireNonNull(ecsWorld, "ecsWorld must not be null");
        Objects.requireNonNull(systems, "systems list must not be null");
        this.systems = List.copyOf(systems);
    }

    /**
     * Executes all systems in order, then increments the world tick.
     */
    public void tick() {
        long currentTick = ecsWorld.getCurrentTick();
        for (GameSystem system : systems) {
            system.tick(currentTick);
        }
        ecsWorld.incrementTick();
    }

    /**
     * Returns the number of registered systems.
     *
     * @return system count
     */
    public int systemCount() {
        return systems.size();
    }
}
