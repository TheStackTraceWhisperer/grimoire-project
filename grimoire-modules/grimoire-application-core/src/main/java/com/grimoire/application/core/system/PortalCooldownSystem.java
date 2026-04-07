package com.grimoire.application.core.system;

import com.grimoire.application.core.ecs.ComponentManager;
import com.grimoire.application.core.ecs.EcsWorld;
import com.grimoire.application.core.ecs.GameSystem;
import com.grimoire.domain.core.component.PortalCooldown;

import java.util.Objects;

import static com.grimoire.application.core.ecs.ComponentManager.BIT_PORTAL_COOLDOWN;

/**
 * Decrements portal cooldown timers and removes expired ones.
 *
 * <p>
 * Iterates the dense active-entity array using a bitwise signature check.
 * </p>
 */
public class PortalCooldownSystem implements GameSystem {

    private static final long REQUIRED_MASK = BIT_PORTAL_COOLDOWN;

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
    public void tick(long currentTick) {
        int[] active = ecsWorld.getActiveEntities();
        int count = ecsWorld.getActiveCount();
        ComponentManager cm = ecsWorld.getComponentManager();
        long[] sigs = cm.getSignatures();
        PortalCooldown[] cooldowns = cm.getPortalCooldowns();

        for (int j = 0; j < count; j++) {
            int i = active[j];
            if ((sigs[i] & REQUIRED_MASK) != REQUIRED_MASK) {
                continue;
            }
            long remaining = cooldowns[i].decrement();
            if (remaining <= 0) {
                cm.removePortalCooldown(i);
            }
        }
    }
}
