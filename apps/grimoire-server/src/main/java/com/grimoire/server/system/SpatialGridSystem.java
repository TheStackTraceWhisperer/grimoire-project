package com.grimoire.server.system;
import io.micronaut.core.annotation.Order;

import com.grimoire.server.component.Position;
import com.grimoire.server.component.Solid;
import com.grimoire.server.component.Zone;
import com.grimoire.server.config.GameConfig;
import com.grimoire.ecs.EcsWorld;
import com.grimoire.ecs.GameSystem;
import com.grimoire.ecs.SpatialGrid;
import jakarta.inject.Singleton;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Maintains the spatial grid for efficient collision detection.
 * 
 * <p>This system runs before movement processing to ensure the grid is up-to-date.
 * It tracks all entities with {@link Solid} and {@link Position} components.</p>
 */
@Order(400)
@Singleton
@Slf4j
public class SpatialGridSystem implements GameSystem {
    
    private static final String DEFAULT_ZONE_ID = "default";
    
    private final EcsWorld ecsWorld;
    
    @Getter
    private final SpatialGrid grid;
    
    public SpatialGridSystem(EcsWorld ecsWorld, GameConfig gameConfig) {
        this.ecsWorld = ecsWorld;
        this.grid = new SpatialGrid(gameConfig.getSpatialGridCellSize());
    }
    
    @Override
    public void tick(float deltaTime) {
        updateGrid();
    }
    
    /**
     * Updates the spatial grid with all solid entities' current positions.
     */
    private void updateGrid() {
        // Collect all solid entities
        List<String> solidEntities = new ArrayList<>();
        for (String entityId : ecsWorld.getEntitiesWithComponent(Solid.class)) {
            solidEntities.add(entityId);
        }
        
        for (String entityId : solidEntities) {
            Optional<Position> posOpt = ecsWorld.getComponent(entityId, Position.class);
            Optional<Zone> zoneOpt = ecsWorld.getComponent(entityId, Zone.class);
            
            if (posOpt.isPresent()) {
                Position pos = posOpt.get();
                String zoneId = zoneOpt.map(Zone::zoneId).orElse(DEFAULT_ZONE_ID);
                grid.updateEntity(entityId, pos.x(), pos.y(), zoneId);
            }
        }
    }
    
    /**
     * Manually removes an entity from the grid (call when entity is destroyed).
     * 
     * @param entityId the entity to remove
     */
    public void removeEntity(String entityId) {
        grid.removeEntity(entityId);
    }
}
