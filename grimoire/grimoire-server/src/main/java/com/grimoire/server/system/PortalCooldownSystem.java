package com.grimoire.server.system;

import com.grimoire.server.component.PortalCooldown;
import com.grimoire.server.ecs.EcsWorld;
import com.grimoire.server.ecs.GameSystem;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;

/**
 * Manages portal cooldown timers.
 */
@Singleton
@RequiredArgsConstructor
public class PortalCooldownSystem implements GameSystem {
    
    private final EcsWorld ecsWorld;
    
    @Override
    public void tick(float deltaTime) {
        for (String entityId : ecsWorld.getEntitiesWithComponent(PortalCooldown.class)) {
            ecsWorld.getComponent(entityId, PortalCooldown.class).ifPresent(cooldown -> {
                long newTicks = cooldown.ticksRemaining() - 1;
                if (newTicks <= 0) {
                    ecsWorld.removeComponent(entityId, PortalCooldown.class);
                } else {
                    ecsWorld.addComponent(entityId, new PortalCooldown(newTicks));
                }
            });
        }
    }
}
