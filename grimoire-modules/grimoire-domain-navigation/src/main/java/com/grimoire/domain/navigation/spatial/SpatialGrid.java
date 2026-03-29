package com.grimoire.domain.navigation.spatial;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Spatial partitioning grid for efficient proximity queries.
 *
 * <p>
 * Entities are partitioned into cells based on position. Queries return
 * entities in the same cell and its 8 neighbors, reducing proximity checks from
 * O(N²) to O(N·k) where k is the average entities per cell.
 * </p>
 *
 * <p>
 * <strong>Thread-safety:</strong> This class must only be accessed from a
 * single thread. No internal synchronization is provided.
 * </p>
 */
public final class SpatialGrid {

    /** The size of each spatial cell in world units. */
    private final int cellSize;

    /** Maps cell keys to the set of entity IDs in that cell. */
    private final Map<CellKey, Set<String>> cells;

    /** Maps entity IDs to their current cell key. */
    private final Map<String, CellKey> entityCells;

    /**
     * Creates a spatial grid with the specified cell size.
     *
     * @param cellSize
     *            the size of each cell in world units
     * @throws IllegalArgumentException
     *             if cellSize is not positive
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
     * @param entityId
     *            the entity ID
     * @param x
     *            the X coordinate
     * @param y
     *            the Y coordinate
     * @param zoneId
     *            the zone (entities in different zones never collide)
     */
    public void updateEntity(String entityId, double x, double y, String zoneId) {
        CellKey newCell = cellKey(x, y, zoneId);
        CellKey oldCell = entityCells.get(entityId);

        if (newCell.equals(oldCell)) {
            return;
        }
        if (oldCell != null) {
            removeFromCell(entityId, oldCell);
        }
        cells.computeIfAbsent(newCell, key -> new HashSet<>()).add(entityId);
        entityCells.put(entityId, newCell);
    }

    /**
     * Removes an entity from the grid.
     *
     * @param entityId
     *            the entity ID
     */
    public void removeEntity(String entityId) {
        CellKey cell = entityCells.remove(entityId);
        if (cell != null) {
            removeFromCell(entityId, cell);
        }
    }

    /**
     * Returns all entity IDs in the same or neighboring cells.
     *
     * @param x
     *            the query X coordinate
     * @param y
     *            the query Y coordinate
     * @param zoneId
     *            the zone
     * @return set of nearby entity IDs (never null)
     */
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public Set<String> getNearbyEntities(double x, double y, String zoneId) {
        Set<String> result = new HashSet<>();
        int cx = (int) Math.floor(x / cellSize);
        int cy = (int) Math.floor(y / cellSize);
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                Set<String> set = cells.get(new CellKey(cx + dx, cy + dy, zoneId));
                if (set != null) {
                    result.addAll(set);
                }
            }
        }
        return result;
    }

    /** Removes all entities from the grid. */
    public void clear() {
        cells.clear();
        entityCells.clear();
    }

    /** Returns the total number of tracked entities. */
    public int getEntityCount() {
        return entityCells.size();
    }

    /** Returns the number of non-empty cells. */
    public int getCellCount() {
        return cells.size();
    }

    private void removeFromCell(String entityId, CellKey cell) {
        Set<String> cellEntities = cells.get(cell);
        if (cellEntities != null) {
            cellEntities.remove(entityId);
            if (cellEntities.isEmpty()) {
                cells.remove(cell);
            }
        }
    }

    private CellKey cellKey(double x, double y, String zoneId) {
        return new CellKey((int) Math.floor(x / cellSize), (int) Math.floor(y / cellSize), zoneId);
    }

    private record CellKey(int x, int y, String zoneId) {
    }
}
