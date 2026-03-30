package com.grimoire.application.core.system;

import com.grimoire.application.core.ecs.EcsWorld;
import com.grimoire.application.core.ecs.GameSystem;
import com.grimoire.domain.core.component.PortalCooldown;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Decrements portal cooldown timers and removes expired ones.
 *
 * <p>
 * After a zone transition, entities receive a {@link PortalCooldown} component
 * to prevent immediate re-entry. This system counts down the remaining ticks
 * and removes the component when it reaches zero.
 * </p>
 */
public class PortalCooldownSystem implements GameSystem {

    /** The ECS world. */
    @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "EcsWorld is a managed collaborator, not external mutable data")
    private final EcsWorld ecsWorld;

    /**
     * Creates a portal cooldown system.
     *
     * @param ecsWorld
     *            the ECS world
     */
    public PortalCooldownSystem(EcsWorld ecsWorld) {
        this.ecsWorld = Objects.requireNonNull(ecsWorld, "ecsWorld must not be null");
    }

    @Override
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public void tick(float deltaTime) {
        List<String> entities = new ArrayList<>();
        for (String entityId : ecsWorld.getEntitiesWithComponent(PortalCooldown.class)) {
            entities.add(entityId);
        }

        for (String entityId : entities) {
            ecsWorld.getComponent(entityId, PortalCooldown.class).ifPresent(cooldown -> {
                long remaining = cooldown.ticksRemaining() - 1;
                if (remaining <= 0) {
                    ecsWorld.removeComponent(entityId, PortalCooldown.class);
                } else {
                    ecsWorld.addComponent(entityId, new PortalCooldown(remaining));
                }
            });
        }
    }
}
