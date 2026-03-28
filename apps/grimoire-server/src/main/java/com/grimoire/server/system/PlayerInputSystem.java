package com.grimoire.server.system;
import io.micronaut.core.annotation.Order;

import com.grimoire.server.component.MovementIntent;
import com.grimoire.server.component.PlayerConnection;
import com.grimoire.server.component.Velocity;
import com.grimoire.server.config.GameConfig;
import com.grimoire.ecs.EcsWorld;
import com.grimoire.ecs.GameSystem;
import jakarta.inject.Singleton;

/**
 * Processes player movement input and updates velocity.
 */
@Order(100)
@Singleton
public class PlayerInputSystem implements GameSystem {
    
    private final EcsWorld ecsWorld;
    private final double playerSpeed;
    
    public PlayerInputSystem(EcsWorld ecsWorld, GameConfig gameConfig) {
        this.ecsWorld = ecsWorld;
        this.playerSpeed = gameConfig.getPlayerSpeed();
    }
    
    @Override
    public void tick(float deltaTime) {
        for (String entityId : ecsWorld.getEntitiesWithComponent(PlayerConnection.class)) {
            ecsWorld.getComponent(entityId, MovementIntent.class).ifPresent(intent -> {
                // Validate intent and calculate velocity
                ecsWorld.getComponent(entityId, com.grimoire.server.component.Position.class).ifPresent(pos -> {
                    double dx = intent.targetX() - pos.x();
                    double dy = intent.targetY() - pos.y();
                    double distance = Math.sqrt(dx * dx + dy * dy);
                    
                    if (distance > 0.1) {
                        // Normalize and apply configurable speed
                        double vx = (dx / distance) * playerSpeed;
                        double vy = (dy / distance) * playerSpeed;
                        ecsWorld.addComponent(entityId, new Velocity(vx, vy));
                    } else {
                        // Reached destination, stop moving
                        ecsWorld.addComponent(entityId, new Velocity(0, 0));
                    }
                });
                
                // Remove the intent after processing
                ecsWorld.removeComponent(entityId, MovementIntent.class);
            });
        }
    }
}
