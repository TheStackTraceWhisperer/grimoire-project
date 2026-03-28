package com.grimoire.ecs;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Spatial partitioning grid for efficient collision detection.
 * 
 * <p>Entities are partitioned into grid cells based on their position. 
 * Collision detection only needs to check entities in the same or neighboring cells,
 * reducing complexity from O(N²) to O(N*k) where k is the average entities per cell.</p>
 * 
 * <p><strong>Thread-safety:</strong> This class must only be accessed from the single-threaded 
 * game loop. Per project guidelines, ECS data should not use synchronized blocks or 
 * concurrent collections; consistency is ensured by the game loop's single-threaded execution.</p>
 */
@SuppressWarnings({
    "PMD.CommentRequired",
    "PMD.AvoidDeeplyNestedIfStmts",
    "PMD.AvoidInstantiatingObjectsInLoops"
})
public final class SpatialGrid {
    
    private final int cellSize;
    private final Map<CellKey, Set<String>> cells;
    private final Map<String, CellKey> entityCells;
    
    /**
     * Creates a new spatial grid with the specified cell size.
     * 
     * @param cellSize the size of each grid cell in world units
     */
    public SpatialGrid(int cellSize) {
        if (cellSize <= 0) {
            throw new IllegalArgumentException("Cell size must be positive");
        }
        this.cellSize = cellSize;
        this.cells = new HashMap<>();
        this.entityCells = new HashMap<>();
    }
    
    /**
     * Adds or updates an entity's position in the grid.
     * 
     * @param entityId the entity ID
     * @param x the entity's x coordinate
     * @param y the entity's y coordinate
     * @param zoneId the zone ID (entities in different zones don't collide)
     */
    public void updateEntity(String entityId, double x, double y, String zoneId) {
        CellKey newCell = getCellKey(x, y, zoneId);
        CellKey oldCell = entityCells.get(entityId);
        
        if (newCell.equals(oldCell)) {
            return; // No change needed
        }
        
        // Remove from old cell if exists
        if (oldCell != null) {
            Set<String> oldCellEntities = cells.get(oldCell);
            if (oldCellEntities != null) {
                oldCellEntities.remove(entityId);
                if (oldCellEntities.isEmpty()) {
                    cells.remove(oldCell);
                }
            }
        }
        
        // Add to new cell
        cells.computeIfAbsent(newCell, k -> new HashSet<>()).add(entityId);
        entityCells.put(entityId, newCell);
    }
    
    /**
     * Removes an entity from the grid.
     * 
     * @param entityId the entity ID
     */
    public void removeEntity(String entityId) {
        CellKey cell = entityCells.remove(entityId);
        if (cell != null) {
            Set<String> cellEntities = cells.get(cell);
            if (cellEntities != null) {
                cellEntities.remove(entityId);
                if (cellEntities.isEmpty()) {
                    cells.remove(cell);
                }
            }
        }
    }
    
    /**
     * Gets all entity IDs in the same or neighboring cells.
     * 
     * @param x the center x coordinate
     * @param y the center y coordinate
     * @param zoneId the zone ID
     * @return set of entity IDs that could potentially collide
     */
    public Set<String> getNearbyEntities(double x, double y, String zoneId) {
        Set<String> result = new HashSet<>();
        int cellX = (int) Math.floor(x / cellSize);
        int cellY = (int) Math.floor(y / cellSize);
        
        // Check 3x3 grid of cells around the position
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                CellKey neighbor = new CellKey(cellX + dx, cellY + dy, zoneId);
                Set<String> cellEntities = cells.get(neighbor);
                if (cellEntities != null) {
                    result.addAll(cellEntities);
                }
            }
        }
        
        return result;
    }
    
    /**
     * Clears all entities from the grid.
     */
    public void clear() {
        cells.clear();
        entityCells.clear();
    }
    
    /**
     * Gets the number of entities in the grid.
     * 
     * @return the total entity count
     */
    public int getEntityCount() {
        return entityCells.size();
    }
    
    /**
     * Gets the number of active cells.
     * 
     * @return the cell count
     */
    public int getCellCount() {
        return cells.size();
    }
    
    private CellKey getCellKey(double x, double y, String zoneId) {
        int cellX = (int) Math.floor(x / cellSize);
        int cellY = (int) Math.floor(y / cellSize);
        return new CellKey(cellX, cellY, zoneId);
    }
    
    /**
     * A key identifying a specific grid cell.
     */
    private record CellKey(int x, int y, String zoneId) {}
}
