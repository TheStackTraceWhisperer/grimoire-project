package com.grimoire.application.core.system;

import com.grimoire.application.core.ecs.EcsWorld;
import com.grimoire.application.core.ecs.GameSystem;
import com.grimoire.domain.core.component.PortalCooldown;

import java.util.Objects;

/**
 * Decrements portal cooldown timers and removes expired ones.
 *
 * <p>
 * Iterates all entities using a contiguous for-loop over the PortalCooldown
 * array.
 * </p>
 */
public class PortalCooldownSystem implements GameSystem {

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
    public void tick(float deltaTime) {
        int max = ecsWorld.getMaxEntityId();
        boolean[] alive = ecsWorld.getAlive();
        PortalCooldown[] cooldowns = ecsWorld.getComponentManager().getPortalCooldowns();

        for (int i = 0; i < max; i++) {
            if (!alive[i] || cooldowns[i] == null) {
                continue;
            }
            long remaining = cooldowns[i].decrement();
            if (remaining <= 0) {
                cooldowns[i] = null;
            }
        }
    }
}
