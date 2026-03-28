package com.grimoire.server.system;
import io.micronaut.core.annotation.Order;

import com.grimoire.server.component.BoundingBox;
import com.grimoire.server.component.Dirty;
import com.grimoire.server.component.Position;
import com.grimoire.server.component.Velocity;
import com.grimoire.server.component.Zone;
import com.grimoire.ecs.EcsWorld;
import com.grimoire.ecs.GameSystem;
import com.grimoire.ecs.SpatialGrid;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;

import java.util.Optional;
import java.util.Set;

/**
 * Updates entity positions based on velocity, with AABB collision detection.
 * 
 * <p>This system processes movement for all entities with {@link Velocity} and
 * {@link Position} components. Collision is checked against entities with both
 * {@link BoundingBox} and {@link Solid} components.</p>
 * 
 * <p>Uses spatial partitioning via {@link SpatialGrid} for O(N*k) collision detection
 * instead of O(N²), where k is the average entities per grid cell.</p>
 */
@Order(500)
@Singleton
@RequiredArgsConstructor
public class MovementSystem implements GameSystem {
    
    private static final String DEFAULT_ZONE_ID = "default";
    
    private final EcsWorld ecsWorld;
    private final SpatialGridSystem spatialGridSystem;
    
    @Override
    public void tick(float deltaTime) {
        for (String entityId : ecsWorld.getEntitiesWithComponent(Velocity.class)) {
            var velocityOpt = ecsWorld.getComponent(entityId, Velocity.class);
            var positionOpt = ecsWorld.getComponent(entityId, Position.class);
            
            if (velocityOpt.isPresent() && positionOpt.isPresent()) {
                Velocity velocity = velocityOpt.get();
                Position position = positionOpt.get();
                
                // Only update if there's actual movement
                if (Math.abs(velocity.dx()) > 0.01 || Math.abs(velocity.dy()) > 0.01) {
                    double newX = position.x() + velocity.dx() * deltaTime;
                    double newY = position.y() + velocity.dy() * deltaTime;
                    
                    // Check for collisions with solid entities
                    if (!checkCollision(entityId, newX, newY)) {
                        ecsWorld.addComponent(entityId, new Position(newX, newY));
                        ecsWorld.addComponent(entityId, new Dirty(ecsWorld.getCurrentTick()));
                    } else {
                        // Stop the entity if collision detected
                        ecsWorld.addComponent(entityId, new Velocity(0, 0));
                    }
                }
            }
        }
    }
    
    /**
     * Checks if moving to the proposed position would cause a collision with any solid entity.
     * Uses spatial partitioning for efficient collision detection.
     * 
     * @param movingEntityId the entity that is moving
     * @param newX the proposed X position
     * @param newY the proposed Y position
     * @return true if a collision would occur, false otherwise
     */
    private boolean checkCollision(String movingEntityId, double newX, double newY) {
        // Get the moving entity's bounding box and zone
        Optional<BoundingBox> movingBoxOpt = ecsWorld.getComponent(movingEntityId, BoundingBox.class);
        Optional<Zone> movingZoneOpt = ecsWorld.getComponent(movingEntityId, Zone.class);
        
        if (movingBoxOpt.isEmpty()) {
            // No bounding box means no collision
            return false;
        }
        
        BoundingBox movingBox = movingBoxOpt.get();
        String movingZone = movingZoneOpt.map(Zone::zoneId).orElse(DEFAULT_ZONE_ID);
        
        // Calculate the moving entity's bounding box at the new position
        double movingLeft = newX - movingBox.width() / 2;
        double movingRight = newX + movingBox.width() / 2;
        double movingTop = newY - movingBox.height() / 2;
        double movingBottom = newY + movingBox.height() / 2;
        
        // Use spatial grid to get only nearby solid entities (O(k) instead of O(N))
        SpatialGrid grid = spatialGridSystem.getGrid();
        Set<String> nearbyEntities = grid.getNearbyEntities(newX, newY, movingZone);
        
        for (String solidEntityId : nearbyEntities) {
            // Don't check collision with self
            if (solidEntityId.equals(movingEntityId)) {
                continue;
            }
            
            // Get the solid entity's position and bounding box
            Optional<Position> solidPosOpt = ecsWorld.getComponent(solidEntityId, Position.class);
            Optional<BoundingBox> solidBoxOpt = ecsWorld.getComponent(solidEntityId, BoundingBox.class);
            
            if (solidPosOpt.isEmpty() || solidBoxOpt.isEmpty()) {
                continue;
            }
            
            Position solidPos = solidPosOpt.get();
            BoundingBox solidBox = solidBoxOpt.get();
            
            // Calculate the solid entity's bounding box
            double solidLeft = solidPos.x() - solidBox.width() / 2;
            double solidRight = solidPos.x() + solidBox.width() / 2;
            double solidTop = solidPos.y() - solidBox.height() / 2;
            double solidBottom = solidPos.y() + solidBox.height() / 2;
            
            // AABB collision check
            if (movingLeft < solidRight && movingRight > solidLeft &&
                movingTop < solidBottom && movingBottom > solidTop) {
                return true; // Collision detected
            }
        }
        
        return false;
    }
}

