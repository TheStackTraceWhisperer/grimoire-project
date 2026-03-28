package com.grimoire.server.navigation;

import com.grimoire.server.component.BoundingBox;
import com.grimoire.server.component.Position;
import com.grimoire.server.component.Solid;
import com.grimoire.server.component.Zone;
import com.grimoire.server.config.GameConfig;
import com.grimoire.ecs.EcsWorld;
import com.grimoire.ecs.GameSystem;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * ECS System responsible for maintaining NavigationGrids for zones.
 * 
 * <p>This system dynamically builds and updates navigation grids at runtime by
 * iterating over all entities with {@link Solid}, {@link BoundingBox}, and
 * {@link Position} components.</p>
 * 
 * <p>Grids are created on-demand per zone and updated periodically to reflect
 * changes in solid entity positions. Uses a dirty-flag system to only rebuild
 * zones that have had entity changes, improving efficiency.</p>
 * 
 * <p><strong>Thread-safety:</strong> This class must only be accessed from the single-threaded 
 * game loop. Per project guidelines, ECS data should not use synchronized blocks or 
 * concurrent collections; consistency is ensured by the game loop's single-threaded execution.</p>
 */
@Singleton
@Slf4j
public class NavigationSystem implements GameSystem {
    
    private static final String DEFAULT_ZONE_ID = "default";
    private static final int UPDATE_INTERVAL_TICKS = 10; // Update every 10 ticks (0.5 seconds at 20 TPS)
    
    private final EcsWorld ecsWorld;
    private final int navigationTileSize;
    private final int defaultZoneWidth;
    private final int defaultZoneHeight;
    private final Map<String, NavigationGrid> zoneGrids;
    private final Set<String> dirtyZones;
    private final Map<String, Set<String>> zoneEntityCache; // Track which entities are in each zone
    private long tickCounter = 0;
    
    /**
     * Creates a new NavigationSystem.
     * 
     * @param ecsWorld the ECS world
     * @param gameConfig the game configuration
     */
    public NavigationSystem(EcsWorld ecsWorld, GameConfig gameConfig) {
        this.ecsWorld = ecsWorld;
        // Use spatial grid cell size as a base, but navigation uses finer tiles
        this.navigationTileSize = NavigationGrid.DEFAULT_TILE_SIZE;
        // Default zone size - could be made configurable
        this.defaultZoneWidth = 2048;
        this.defaultZoneHeight = 2048;
        this.zoneGrids = new HashMap<>();
        this.dirtyZones = new HashSet<>();
        this.zoneEntityCache = new HashMap<>();
    }
    
    @Override
    public void tick(float deltaTime) {
        tickCounter++;
        
        // Only update grids periodically to reduce CPU cost
        if (tickCounter % UPDATE_INTERVAL_TICKS == 0) {
            detectChangesAndMarkDirty();
            rebuildDirtyGrids();
        }
    }
    
    /**
     * Detects changes in solid entities and marks affected zones as dirty.
     * 
     * <p>Compares current entity state against cached state to determine which
     * zones need to be rebuilt.</p>
     */
    private void detectChangesAndMarkDirty() {
        Map<String, Set<String>> currentZoneEntities = new HashMap<>();
        
        // Build current state of zone -> entities mapping
        for (String entityId : ecsWorld.getEntitiesWithComponent(Solid.class)) {
            var posOpt = ecsWorld.getComponent(entityId, Position.class);
            var bbOpt = ecsWorld.getComponent(entityId, BoundingBox.class);
            
            if (posOpt.isEmpty() || bbOpt.isEmpty()) {
                continue;
            }
            
            String zoneId = ecsWorld.getComponent(entityId, Zone.class)
                    .map(Zone::zoneId)
                    .orElse(DEFAULT_ZONE_ID);
            
            currentZoneEntities.computeIfAbsent(zoneId, k -> new HashSet<>()).add(entityId);
        }
        
        // Check for zones with changed entities
        Set<String> allZones = new HashSet<>();
        allZones.addAll(currentZoneEntities.keySet());
        allZones.addAll(zoneEntityCache.keySet());
        
        for (String zoneId : allZones) {
            Set<String> currentEntities = currentZoneEntities.getOrDefault(zoneId, Set.of());
            Set<String> cachedEntities = zoneEntityCache.getOrDefault(zoneId, Set.of());
            
            // Mark zone as dirty if entities have changed
            if (!currentEntities.equals(cachedEntities)) {
                dirtyZones.add(zoneId);
            }
        }
        
        // Update cache with current state
        zoneEntityCache.clear();
        zoneEntityCache.putAll(currentZoneEntities);
    }
    
    /**
     * Rebuilds only the navigation grids that have been marked as dirty.
     */
    private void rebuildDirtyGrids() {
        for (String zoneId : dirtyZones) {
            rebuildGrid(zoneId);
            log.debug("Rebuilt navigation grid for zone: {}", zoneId);
        }
        dirtyZones.clear();
    }
    
    /**
     * Marks a specific zone as dirty, requiring a rebuild on next update.
     * 
     * @param zoneId the zone ID to mark as dirty
     */
    public void markZoneDirty(String zoneId) {
        dirtyZones.add(zoneId);
    }
    
    /**
     * Rebuilds all navigation grids from scratch.
     * 
     * <p>This method clears all existing grids and repopulates them based on
     * the current positions of all solid entities with bounding boxes.</p>
     */
    public void rebuildAllGrids() {
        // Clear all existing grids
        for (NavigationGrid grid : zoneGrids.values()) {
            grid.clear();
        }
        
        // Clear dirty tracking since we're rebuilding everything
        dirtyZones.clear();
        zoneEntityCache.clear();
        
        // Iterate over all solid entities
        for (String entityId : ecsWorld.getEntitiesWithComponent(Solid.class)) {
            var posOpt = ecsWorld.getComponent(entityId, Position.class);
            var bbOpt = ecsWorld.getComponent(entityId, BoundingBox.class);
            
            if (posOpt.isEmpty() || bbOpt.isEmpty()) {
                continue; // Skip entities without required components
            }
            
            Position pos = posOpt.get();
            BoundingBox bb = bbOpt.get();
            String zoneId = ecsWorld.getComponent(entityId, Zone.class)
                    .map(Zone::zoneId)
                    .orElse(DEFAULT_ZONE_ID);
            
            NavigationGrid grid = getOrCreateGrid(zoneId);
            grid.markAreaBlocked(pos.x(), pos.y(), bb.width(), bb.height());
            
            // Update cache
            zoneEntityCache.computeIfAbsent(zoneId, k -> new HashSet<>()).add(entityId);
        }
    }
    
    /**
     * Gets the navigation grid for a specific zone.
     * 
     * @param zoneId the zone ID
     * @return the navigation grid for the zone, or null if not yet created
     */
    public NavigationGrid getGrid(String zoneId) {
        return zoneGrids.get(zoneId);
    }
    
    /**
     * Gets or creates the navigation grid for a specific zone.
     * 
     * @param zoneId the zone ID
     * @return the navigation grid for the zone
     */
    public NavigationGrid getOrCreateGrid(String zoneId) {
        return zoneGrids.computeIfAbsent(zoneId, 
                id -> new NavigationGrid(defaultZoneWidth, defaultZoneHeight, navigationTileSize));
    }
    
    /**
     * Forces an immediate rebuild of a specific zone's grid.
     * 
     * @param zoneId the zone ID to rebuild
     */
    public void rebuildGrid(String zoneId) {
        NavigationGrid grid = getOrCreateGrid(zoneId);
        grid.clear();
        
        for (String entityId : ecsWorld.getEntitiesWithComponent(Solid.class)) {
            var zoneOpt = ecsWorld.getComponent(entityId, Zone.class);
            String entityZone = zoneOpt.map(Zone::zoneId).orElse(DEFAULT_ZONE_ID);
            
            if (!entityZone.equals(zoneId)) {
                continue;
            }
            
            var posOpt = ecsWorld.getComponent(entityId, Position.class);
            var bbOpt = ecsWorld.getComponent(entityId, BoundingBox.class);
            
            if (posOpt.isEmpty() || bbOpt.isEmpty()) {
                continue;
            }
            
            Position pos = posOpt.get();
            BoundingBox bb = bbOpt.get();
            grid.markAreaBlocked(pos.x(), pos.y(), bb.width(), bb.height());
        }
    }
    
    /**
     * Removes the navigation grid for a specific zone.
     * 
     * @param zoneId the zone ID
     */
    public void removeGrid(String zoneId) {
        zoneGrids.remove(zoneId);
        dirtyZones.remove(zoneId);
        zoneEntityCache.remove(zoneId);
    }
    
    /**
     * Gets the number of active zone grids.
     * 
     * @return the count of zone grids
     */
    public int getGridCount() {
        return zoneGrids.size();
    }
    
    /**
     * Gets the number of dirty zones pending rebuild.
     * 
     * @return the count of dirty zones
     */
    public int getDirtyZoneCount() {
        return dirtyZones.size();
    }
}
