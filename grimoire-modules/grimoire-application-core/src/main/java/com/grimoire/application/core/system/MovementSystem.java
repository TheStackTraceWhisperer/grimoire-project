package com.grimoire.application.core.system;

import com.grimoire.application.core.ecs.ComponentManager;
import com.grimoire.application.core.ecs.EcsWorld;
import com.grimoire.application.core.ecs.GameSystem;
import com.grimoire.domain.core.component.BoundingBox;
import com.grimoire.domain.core.component.Dirty;
import com.grimoire.domain.core.component.Position;
import com.grimoire.domain.core.component.Velocity;
import com.grimoire.domain.core.component.Zone;
import com.grimoire.domain.navigation.spatial.SpatialGrid;

import java.util.Objects;
import java.util.Set;

/**
 * Updates entity positions based on velocity, with AABB collision detection.
 *
 * <p>
 * Iterates all entities with {@link Velocity} and {@link Position} using a
 * contiguous for-loop over the component arrays.
 * </p>
 */
public class MovementSystem implements GameSystem {

    private static final String DEFAULT_ZONE_ID = "default";
    private static final double MOVEMENT_THRESHOLD = 0.01;

    private final EcsWorld ecsWorld;

    private final SpatialGridSystem spatialGridSystem;

    /**
     * Creates a movement system.
     *
     * @param ecsWorld
     *            the ECS world
     * @param spatialGridSystem
     *            the spatial grid system for collision queries
     */
    public MovementSystem(EcsWorld ecsWorld, SpatialGridSystem spatialGridSystem) {
        this.ecsWorld = Objects.requireNonNull(ecsWorld, "ecsWorld must not be null");
        this.spatialGridSystem = Objects.requireNonNull(spatialGridSystem,
                "spatialGridSystem must not be null");
    }

    @Override
    public void tick(float deltaTime) {
        int max = ecsWorld.getMaxEntityId();
        boolean[] alive = ecsWorld.getAlive();
        ComponentManager cm = ecsWorld.getComponentManager();
        Velocity[] velocities = cm.getVelocities();
        Position[] positions = cm.getPositions();

        for (int i = 0; i < max; i++) {
            if (!alive[i] || velocities[i] == null || positions[i] == null) {
                continue;
            }
            processMovement(i, velocities[i], positions[i], deltaTime, cm);
        }
    }

    private void processMovement(int entityId, Velocity velocity, Position position,
            float deltaTime, ComponentManager cm) {
        if (Math.abs(velocity.dx) > MOVEMENT_THRESHOLD
                || Math.abs(velocity.dy) > MOVEMENT_THRESHOLD) {
            applyMovement(entityId, velocity, position, deltaTime, cm);
        }
    }

    private void applyMovement(int entityId, Velocity velocity, Position position,
            float deltaTime, ComponentManager cm) {
        double newX = position.x + velocity.dx * deltaTime;
        double newY = position.y + velocity.dy * deltaTime;

        if (checkCollision(entityId, newX, newY, cm)) {
            velocity.update(0, 0);
        } else {
            position.update(newX, newY);
            Dirty dirty = cm.getDirties()[entityId];
            if (dirty == null) {
                cm.addComponent(entityId, new Dirty(ecsWorld.getCurrentTick()));
            } else {
                dirty.tick = ecsWorld.getCurrentTick();
            }
        }
    }

    private boolean checkCollision(int movingEntityId, double newX, double newY,
            ComponentManager cm) {
        BoundingBox movingBox = cm.getBoundingBoxes()[movingEntityId];
        if (movingBox == null) {
            return false;
        }

        Zone movingZoneComp = cm.getZones()[movingEntityId];
        String movingZone = movingZoneComp != null ? movingZoneComp.zoneId : DEFAULT_ZONE_ID;

        double movingLeft = newX - movingBox.width / 2;
        double movingRight = newX + movingBox.width / 2;
        double movingTop = newY - movingBox.height / 2;
        double movingBottom = newY + movingBox.height / 2;

        SpatialGrid grid = spatialGridSystem.getGrid();
        Set<Integer> nearbyEntities = grid.getNearbyEntities(newX, newY, movingZone);

        for (int solidEntityId : nearbyEntities) {
            if (solidEntityId == movingEntityId) {
                continue;
            }
            if (cm.getSolids()[solidEntityId] == null) {
                continue;
            }

            Position solidPos = cm.getPositions()[solidEntityId];
            BoundingBox solidBox = cm.getBoundingBoxes()[solidEntityId];
            if (solidPos == null || solidBox == null) {
                continue;
            }

            double solidLeft = solidPos.x - solidBox.width / 2;
            double solidRight = solidPos.x + solidBox.width / 2;
            double solidTop = solidPos.y - solidBox.height / 2;
            double solidBottom = solidPos.y + solidBox.height / 2;

            if (movingLeft < solidRight && movingRight > solidLeft
                    && movingTop < solidBottom && movingBottom > solidTop) {
                return true;
            }
        }
        return false;
    }
}
