package com.grimoire.ecs;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.util.Collection;
import java.util.List;

/**
 * Schedules and executes all registered {@link GameSystem} instances in a fixed, declarative order.
 *
 * <p>Micronaut injects all beans that implement {@link GameSystem}. Each system should carry
 * an {@code @io.micronaut.core.annotation.Order(n)} annotation to guarantee deterministic
 * execution ordering across ticks.</p>
 */
@Singleton
@SuppressWarnings("PMD.CommentRequired")
public class SystemScheduler {

    private final EcsWorld ecsWorld;
    private final List<GameSystem> systems;

    /**
     * Constructs the scheduler with all game systems provided by the Micronaut bean context.
     *
     * @param ecsWorld the shared ECS world
     * @param systems  all game-system beans, sorted by {@code @Order}
     */
    @Inject
    @SuppressFBWarnings(
        value = "EI_EXPOSE_REP2",
        justification = "EcsWorld is a DI-managed shared runtime state container intentionally passed by reference."
    )
    public SystemScheduler(EcsWorld ecsWorld, Collection<GameSystem> systems) {
        this.ecsWorld = ecsWorld;
        this.systems = List.copyOf(systems);
    }

    /**
     * Executes all game systems in declared order and advances the world tick counter.
     *
     * @param deltaTime seconds elapsed since the previous tick
     */
    public void tick(float deltaTime) {
        for (GameSystem system : systems) {
            system.tick(deltaTime);
        }
        ecsWorld.incrementTick();
    }
}

