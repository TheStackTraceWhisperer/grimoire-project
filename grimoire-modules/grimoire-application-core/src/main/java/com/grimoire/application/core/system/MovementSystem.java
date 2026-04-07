package com.grimoire.application.core.system;

import com.grimoire.application.core.ecs.ComponentManager;
import com.grimoire.application.core.ecs.EcsWorld;
import com.grimoire.application.core.ecs.GameSystem;
import com.grimoire.domain.core.component.BoundingBox;
import com.grimoire.domain.core.component.Position;
import com.grimoire.domain.core.component.Velocity;
import com.grimoire.domain.core.component.Zone;
import com.grimoire.domain.navigation.spatial.SpatialGrid;

import java.util.Objects;
import java.util.Set;

import static com.grimoire.application.core.ecs.ComponentManager.BIT_POSITION;
import static com.grimoire.application.core.ecs.ComponentManager.BIT_VELOCITY;

/**
 * Updates entity positions based on velocity, with AABB collision detection.
 *
 * <p>
 * Iterates the dense active-entity array using bitwise signature checks for
 * entities with {@link Velocity} and {@link Position}.
 * </p>
 */
public class MovementSystem implements GameSystem {

    private static final String DEFAULT_ZONE_ID = "default";
    private static final double MOVEMENT_THRESHOLD = 0.01;

    /**
     * Fixed delta per tick (50 ms at 20 Hz).
     */
    private static final float TICK_DELTA = 0.05f;

    private static final long REQUIRED_MASK = BIT_VELOCITY | BIT_POSITION;

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
    public void tick(long currentTick) {
        int[] active = ecsWorld.getActiveEntities();
        int count = ecsWorld.getActiveCount();
        ComponentManager cm = ecsWorld.getComponentManager();
        long[] sigs = cm.getSignatures();
        Velocity[] velocities = cm.getVelocities();
        Position[] positions = cm.getPositions();

        for (int j = 0; j < count; j++) {
            int i = active[j];
            if ((sigs[i] & REQUIRED_MASK) != REQUIRED_MASK) {
                continue;
            }
            processMovement(i, velocities[i], positions[i], cm);
        }
    }

    private void processMovement(int entityId, Velocity velocity, Position position,
            ComponentManager cm) {
        if (Math.abs(velocity.dx) > MOVEMENT_THRESHOLD
                || Math.abs(velocity.dy) > MOVEMENT_THRESHOLD) {
            applyMovement(entityId, velocity, position, cm);
        }
    }

    private void applyMovement(int entityId, Velocity velocity, Position position,
            ComponentManager cm) {
        double newX = position.x + velocity.dx * TICK_DELTA;
        double newY = position.y + velocity.dy * TICK_DELTA;

        if (checkCollision(entityId, newX, newY, cm)) {
            velocity.update(0, 0);
        } else {
            position.update(newX, newY);
            cm.addDirty(entityId, ecsWorld.getCurrentTick());
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
