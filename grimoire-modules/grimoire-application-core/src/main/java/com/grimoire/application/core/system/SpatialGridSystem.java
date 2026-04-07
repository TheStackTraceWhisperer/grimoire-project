package com.grimoire.application.core.system;

import com.grimoire.application.core.ecs.EcsWorld;
import com.grimoire.application.core.ecs.GameSystem;
import com.grimoire.application.core.port.GameConfig;
import com.grimoire.domain.core.component.Position;
import com.grimoire.domain.core.component.Zone;
import com.grimoire.domain.navigation.spatial.SpatialGrid;

import java.util.Objects;

import static com.grimoire.application.core.ecs.ComponentManager.BIT_POSITION;
import static com.grimoire.application.core.ecs.ComponentManager.BIT_SOLID;

/**
 * Maintains the {@link SpatialGrid} for efficient proximity queries.
 *
 * <p>
 * Rebuilds the spatial grid each tick from all entities with {@code Solid} and
 * {@code Position} components using the dense active-entity array and bitwise
 * signature checks.
 * </p>
 */
public class SpatialGridSystem implements GameSystem {

    /**
     * Default zone ID for entities without a Zone component.
     */
    private static final String DEFAULT_ZONE_ID = "default";

    private static final long REQUIRED_MASK = BIT_SOLID | BIT_POSITION;

    /**
     * The ECS world.
     */
    private final EcsWorld ecsWorld;

    /**
     * The spatial grid instance rebuilt each tick.
     */
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
    public void tick(long currentTick) {
        grid.clear();

        int[] active = ecsWorld.getActiveEntities();
        int count = ecsWorld.getActiveCount();
        long[] sigs = ecsWorld.getComponentManager().getSignatures();
        Position[] positions = ecsWorld.getComponentManager().getPositions();
        Zone[] zones = ecsWorld.getComponentManager().getZones();

        for (int j = 0; j < count; j++) {
            int i = active[j];
            if ((sigs[i] & REQUIRED_MASK) != REQUIRED_MASK) {
                continue;
            }
            Position pos = positions[i];
            String zoneId = zones[i] != null ? zones[i].zoneId : DEFAULT_ZONE_ID;
            grid.updateEntity(i, pos.x, pos.y, zoneId);
        }
    }

    /**
     * Returns the spatial grid for proximity queries.
     *
     * @return the spatial grid
     */
    public SpatialGrid getGrid() {
        return grid;
    }

    /**
     * Removes an entity from the grid.
     *
     * @param entityId
     *            the entity to remove
     */
    public void removeEntity(int entityId) {
        grid.removeEntity(entityId);
    }
}
