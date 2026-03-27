package com.grimoire.server.navigation;

import java.util.BitSet;

/**
 * Represents the walkable surface of a zone as a grid of nodes.
 * 
 * <p>Each node in the grid is binary: either WALKABLE or BLOCKED.
 * Uses a {@link BitSet} for memory-efficient storage where:
 * <ul>
 *   <li>{@code true} (bit set) = BLOCKED</li>
 *   <li>{@code false} (bit clear) = WALKABLE</li>
 * </ul>
 * </p>
 * 
 * <p><strong>Thread-safety:</strong> This class must only be accessed from the single-threaded 
 * game loop. Per project guidelines, ECS data should not use synchronized blocks or 
 * concurrent collections; consistency is ensured by the game loop's single-threaded execution.</p>
 */
public class NavigationGrid {
    
    /**
     * Default tile size in world units (pixels).
     */
    public static final int DEFAULT_TILE_SIZE = 32;
    
    private final int tileSize;
    private final int width;
    private final int height;
    private final int gridWidth;
    private final int gridHeight;
    private final BitSet blocked;
    
    /**
     * Creates a new navigation grid with the specified dimensions.
     * 
     * @param width the width of the zone in world units
     * @param height the height of the zone in world units
     * @param tileSize the size of each grid tile in world units
     * @throws IllegalArgumentException if any dimension or tile size is not positive
     */
    public NavigationGrid(int width, int height, int tileSize) {
        if (width <= 0) {
            throw new IllegalArgumentException("Width must be positive: " + width);
        }
        if (height <= 0) {
            throw new IllegalArgumentException("Height must be positive: " + height);
        }
        if (tileSize <= 0) {
            throw new IllegalArgumentException("Tile size must be positive: " + tileSize);
        }
        
        this.width = width;
        this.height = height;
        this.tileSize = tileSize;
        this.gridWidth = (int) Math.ceil((double) width / tileSize);
        this.gridHeight = (int) Math.ceil((double) height / tileSize);
        this.blocked = new BitSet(gridWidth * gridHeight);
    }
    
    /**
     * Creates a new navigation grid with the default tile size.
     * 
     * @param width the width of the zone in world units
     * @param height the height of the zone in world units
     */
    public NavigationGrid(int width, int height) {
        this(width, height, DEFAULT_TILE_SIZE);
    }
    
    /**
     * Converts world coordinates to grid indices.
     * 
     * @param x the world X coordinate
     * @param y the world Y coordinate
     * @return a two-element array [gridX, gridY]
     */
    public int[] worldToGrid(double x, double y) {
        int gridX = (int) Math.floor(x / tileSize);
        int gridY = (int) Math.floor(y / tileSize);
        return new int[]{gridX, gridY};
    }
    
    /**
     * Returns the center world coordinate of a grid cell.
     * 
     * @param gridX the grid X index
     * @param gridY the grid Y index
     * @return a two-element array [worldX, worldY] representing the center of the cell
     */
    public double[] gridToWorld(int gridX, int gridY) {
        double worldX = (gridX + 0.5) * tileSize;
        double worldY = (gridY + 0.5) * tileSize;
        return new double[]{worldX, worldY};
    }
    
    /**
     * Checks if a grid cell is blocked.
     * 
     * @param gridX the grid X index
     * @param gridY the grid Y index
     * @return true if the cell is blocked or out of bounds, false if walkable
     */
    public boolean isBlocked(int gridX, int gridY) {
        if (!isValidCell(gridX, gridY)) {
            return true; // Out of bounds is treated as blocked
        }
        return blocked.get(gridToIndex(gridX, gridY));
    }
    
    /**
     * Checks if a grid cell is walkable.
     * 
     * @param gridX the grid X index
     * @param gridY the grid Y index
     * @return true if the cell is walkable, false if blocked or out of bounds
     */
    public boolean isWalkable(int gridX, int gridY) {
        if (!isValidCell(gridX, gridY)) {
            return false; // Out of bounds is not walkable
        }
        return !blocked.get(gridToIndex(gridX, gridY));
    }
    
    /**
     * Sets a grid cell as blocked.
     * 
     * @param gridX the grid X index
     * @param gridY the grid Y index
     */
    public void setBlocked(int gridX, int gridY) {
        if (isValidCell(gridX, gridY)) {
            blocked.set(gridToIndex(gridX, gridY));
        }
    }
    
    /**
     * Sets a grid cell as walkable (clears the blocked state).
     * 
     * @param gridX the grid X index
     * @param gridY the grid Y index
     */
    public void setWalkable(int gridX, int gridY) {
        if (isValidCell(gridX, gridY)) {
            blocked.clear(gridToIndex(gridX, gridY));
        }
    }
    
    /**
     * Marks all tiles within a rectangular area as blocked.
     * 
     * @param worldX the world X coordinate (center of bounding box)
     * @param worldY the world Y coordinate (center of bounding box)
     * @param bbWidth the bounding box width
     * @param bbHeight the bounding box height
     */
    public void markAreaBlocked(double worldX, double worldY, double bbWidth, double bbHeight) {
        double halfWidth = bbWidth / 2.0;
        double halfHeight = bbHeight / 2.0;
        
        int minGridX = (int) Math.floor((worldX - halfWidth) / tileSize);
        int maxGridX = (int) Math.floor((worldX + halfWidth) / tileSize);
        int minGridY = (int) Math.floor((worldY - halfHeight) / tileSize);
        int maxGridY = (int) Math.floor((worldY + halfHeight) / tileSize);
        
        for (int gx = minGridX; gx <= maxGridX; gx++) {
            for (int gy = minGridY; gy <= maxGridY; gy++) {
                setBlocked(gx, gy);
            }
        }
    }
    
    /**
     * Marks all tiles within a rectangular area as walkable.
     * 
     * @param worldX the world X coordinate (center of bounding box)
     * @param worldY the world Y coordinate (center of bounding box)
     * @param bbWidth the bounding box width
     * @param bbHeight the bounding box height
     */
    public void markAreaWalkable(double worldX, double worldY, double bbWidth, double bbHeight) {
        double halfWidth = bbWidth / 2.0;
        double halfHeight = bbHeight / 2.0;
        
        int minGridX = (int) Math.floor((worldX - halfWidth) / tileSize);
        int maxGridX = (int) Math.floor((worldX + halfWidth) / tileSize);
        int minGridY = (int) Math.floor((worldY - halfHeight) / tileSize);
        int maxGridY = (int) Math.floor((worldY + halfHeight) / tileSize);
        
        for (int gx = minGridX; gx <= maxGridX; gx++) {
            for (int gy = minGridY; gy <= maxGridY; gy++) {
                setWalkable(gx, gy);
            }
        }
    }
    
    /**
     * Clears all blocked tiles, making the entire grid walkable.
     */
    public void clear() {
        blocked.clear();
    }
    
    /**
     * Checks if a cell is within the grid bounds.
     * 
     * @param gridX the grid X index
     * @param gridY the grid Y index
     * @return true if the cell is within bounds
     */
    public boolean isValidCell(int gridX, int gridY) {
        return gridX >= 0 && gridX < gridWidth && gridY >= 0 && gridY < gridHeight;
    }
    
    /**
     * Gets the tile size.
     * 
     * @return the tile size in world units
     */
    public int getTileSize() {
        return tileSize;
    }
    
    /**
     * Gets the grid width in cells.
     * 
     * @return the number of columns in the grid
     */
    public int getGridWidth() {
        return gridWidth;
    }
    
    /**
     * Gets the grid height in cells.
     * 
     * @return the number of rows in the grid
     */
    public int getGridHeight() {
        return gridHeight;
    }
    
    /**
     * Gets the number of blocked cells.
     * 
     * @return the count of blocked cells
     */
    public int getBlockedCount() {
        return blocked.cardinality();
    }
    
    /**
     * Converts grid coordinates to a linear index.
     */
    private int gridToIndex(int gridX, int gridY) {
        return gridY * gridWidth + gridX;
    }
}
