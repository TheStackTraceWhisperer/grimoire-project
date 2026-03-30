package com.grimoire.application.core.system;

import com.grimoire.application.core.ecs.EcsWorld;
import com.grimoire.application.core.ecs.GameSystem;
import com.grimoire.domain.core.component.BoundingBox;
import com.grimoire.domain.core.component.Dirty;
import com.grimoire.domain.core.component.Position;
import com.grimoire.domain.core.component.Solid;
import com.grimoire.domain.core.component.Velocity;
import com.grimoire.domain.core.component.Zone;
import com.grimoire.domain.navigation.spatial.SpatialGrid;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Updates entity positions based on velocity, with AABB collision detection.
 *
 * <p>
 * For each entity with {@link Velocity} and {@link Position}, the system
 * computes the candidate position and checks for collisions with nearby
 * {@link Solid} entities via {@link SpatialGrid}. If a collision would occur
 * the entity is stopped; otherwise the position is updated and a {@link Dirty}
 * marker is added for network synchronisation.
 * </p>
 */
public class MovementSystem implements GameSystem {

    /** Default zone ID for entities without a Zone component. */
    private static final String DEFAULT_ZONE_ID = "default";
    /** Minimum velocity magnitude to trigger movement. */
    private static final double MOVEMENT_THRESHOLD = 0.01;

    /** The ECS world. */
    @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "EcsWorld is a managed collaborator, not external mutable data")
    private final EcsWorld ecsWorld;

    /** Spatial grid system for collision queries. */
    @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "SpatialGridSystem is a managed collaborator")
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
        for (String entityId : ecsWorld.getEntitiesWithComponent(Velocity.class)) {
            Optional<Velocity> velOpt = ecsWorld.getComponent(entityId, Velocity.class);
            Optional<Position> posOpt = ecsWorld.getComponent(entityId, Position.class);

            if (velOpt.isPresent() && posOpt.isPresent()) {
                processMovement(entityId, velOpt.get(), posOpt.get(), deltaTime);
            }
        }
    }

    /**
     * Processes movement for a single entity.
     *
     * @param entityId
     *            the entity ID
     * @param velocity
     *            the entity's velocity
     * @param position
     *            the entity's current position
     * @param deltaTime
     *            time elapsed since last tick
     */
    private void processMovement(String entityId, Velocity velocity, Position position,
            float deltaTime) {
        if (Math.abs(velocity.dx()) > MOVEMENT_THRESHOLD
                || Math.abs(velocity.dy()) > MOVEMENT_THRESHOLD) {
            applyMovement(entityId, velocity, position, deltaTime);
        }
    }

    /**
     * Computes the new position and applies it if no collision is detected.
     *
     * @param entityId
     *            the entity ID
     * @param velocity
     *            the entity's velocity
     * @param position
     *            the entity's current position
     * @param deltaTime
     *            time elapsed since last tick
     */
    private void applyMovement(String entityId, Velocity velocity, Position position,
            float deltaTime) {
        double newX = position.x() + velocity.dx() * deltaTime;
        double newY = position.y() + velocity.dy() * deltaTime;

        if (checkCollision(entityId, newX, newY)) {
            ecsWorld.addComponent(entityId, new Velocity(0, 0));
        } else {
            ecsWorld.addComponent(entityId, new Position(newX, newY));
            ecsWorld.addComponent(entityId, new Dirty(ecsWorld.getCurrentTick()));
        }
    }

    /**
     * Checks whether moving to the proposed position would cause an AABB collision
     * with any nearby {@link Solid} entity.
     *
     * @param movingEntityId
     *            the entity that is moving
     * @param newX
     *            proposed X position
     * @param newY
     *            proposed Y position
     * @return {@code true} if a collision would occur
     */
    @SuppressWarnings("PMD.CyclomaticComplexity")
    private boolean checkCollision(String movingEntityId, double newX, double newY) {
        Optional<BoundingBox> movingBoxOpt = ecsWorld.getComponent(movingEntityId,
                BoundingBox.class);
        if (movingBoxOpt.isEmpty()) {
            return false;
        }

        BoundingBox movingBox = movingBoxOpt.get();
        String movingZone = ecsWorld.getComponent(movingEntityId, Zone.class)
                .map(Zone::zoneId)
                .orElse(DEFAULT_ZONE_ID);

        double movingLeft = newX - movingBox.width() / 2;
        double movingRight = newX + movingBox.width() / 2;
        double movingTop = newY - movingBox.height() / 2;
        double movingBottom = newY + movingBox.height() / 2;

        SpatialGrid grid = spatialGridSystem.getGrid();
        Set<String> nearbyEntities = grid.getNearbyEntities(newX, newY, movingZone);

        for (String solidEntityId : nearbyEntities) {
            if (solidEntityId.equals(movingEntityId)) {
                continue;
            }
            if (!ecsWorld.hasComponent(solidEntityId, Solid.class)) {
                continue;
            }

            Optional<Position> solidPosOpt = ecsWorld.getComponent(solidEntityId, Position.class);
            Optional<BoundingBox> solidBoxOpt = ecsWorld.getComponent(solidEntityId,
                    BoundingBox.class);

            if (solidPosOpt.isEmpty() || solidBoxOpt.isEmpty()) {
                continue;
            }

            Position solidPos = solidPosOpt.get();
            BoundingBox solidBox = solidBoxOpt.get();

            double solidLeft = solidPos.x() - solidBox.width() / 2;
            double solidRight = solidPos.x() + solidBox.width() / 2;
            double solidTop = solidPos.y() - solidBox.height() / 2;
            double solidBottom = solidPos.y() + solidBox.height() / 2;

            if (movingLeft < solidRight && movingRight > solidLeft
                    && movingTop < solidBottom && movingBottom > solidTop) {
                return true;
            }
        }

        return false;
    }
}
