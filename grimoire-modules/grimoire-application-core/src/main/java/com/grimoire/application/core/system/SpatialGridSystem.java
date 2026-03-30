package com.grimoire.application.core.system;

import com.grimoire.application.core.ecs.EcsWorld;
import com.grimoire.application.core.ecs.GameSystem;
import com.grimoire.application.core.port.GameConfig;
import com.grimoire.domain.core.component.Position;
import com.grimoire.domain.core.component.Solid;
import com.grimoire.domain.core.component.Zone;
import com.grimoire.domain.navigation.spatial.SpatialGrid;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.util.Objects;
import java.util.Optional;

/**
 * Maintains the {@link SpatialGrid} for efficient proximity queries.
 *
 * <p>
 * This system should be ticked before any system that performs collision or
 * proximity checks (e.g., {@link MovementSystem}, {@link CombatSystem},
 * {@link NpcAiSystem}). It rebuilds the spatial grid each tick from all
 * entities with {@link Solid} and {@link Position} components.
 * </p>
 */
public class SpatialGridSystem implements GameSystem {

    /** Default zone ID for entities without a {@link Zone} component. */
    private static final String DEFAULT_ZONE_ID = "default";

    /** The ECS world. */
    @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "EcsWorld is a managed collaborator, not external mutable data")
    private final EcsWorld ecsWorld;

    /** The spatial grid instance rebuilt each tick. */
    private final SpatialGrid grid;

    /**
     * Creates a spatial grid system.
     *
     * @param ecsWorld
     *            the ECS world
     * @param gameConfig
     *            configuration providing the grid cell size
     */
    public SpatialGridSystem(EcsWorld ecsWorld, GameConfig gameConfig) {
        this.ecsWorld = Objects.requireNonNull(ecsWorld, "ecsWorld must not be null");
        Objects.requireNonNull(gameConfig, "gameConfig must not be null");
        this.grid = new SpatialGrid(gameConfig.spatialGridCellSize());
    }

    @Override
    public void tick(float deltaTime) {
        grid.clear();

        for (String entityId : ecsWorld.getEntitiesWithComponent(Solid.class)) {
            Optional<Position> posOpt = ecsWorld.getComponent(entityId, Position.class);
            if (posOpt.isPresent()) {
                Position pos = posOpt.get();
                String zoneId = ecsWorld.getComponent(entityId, Zone.class)
                        .map(Zone::zoneId)
                        .orElse(DEFAULT_ZONE_ID);
                grid.updateEntity(entityId, pos.x(), pos.y(), zoneId);
            }
        }
    }

    /**
     * Returns the spatial grid for proximity queries.
     *
     * <p>
     * The returned grid is the live instance rebuilt each tick. It is owned by this
     * system and must only be read from the game-loop thread.
     * </p>
     *
     * @return the spatial grid
     */
    @SuppressFBWarnings(value = "EI_EXPOSE_REP", justification = "SpatialGrid is a managed collaborator shared with sibling systems on the same thread")
    public SpatialGrid getGrid() {
        return grid;
    }

    /**
     * Removes an entity from the grid.
     *
     * <p>
     * Call this when an entity is destroyed outside of the normal tick cycle (e.g.,
     * during death processing) to keep the grid consistent.
     * </p>
     *
     * @param entityId
     *            the entity to remove
     */
    public void removeEntity(String entityId) {
        grid.removeEntity(entityId);
    }
}
