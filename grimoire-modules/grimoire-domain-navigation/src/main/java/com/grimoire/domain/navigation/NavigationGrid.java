package com.grimoire.domain.navigation;

import java.util.BitSet;

/**
 * Represents the walkable surface of a zone as a grid of nodes.
 *
 * <p>
 * Each node is binary: either WALKABLE or BLOCKED. Uses a {@link BitSet} for
 * memory-efficient storage where {@code true} (bit set) = BLOCKED,
 * {@code false} (bit clear) = WALKABLE.
 * </p>
 *
 * <p>
 * <strong>Thread-safety:</strong> This class must only be accessed from a
 * single thread (e.g., the game loop). No internal synchronization is provided.
 * </p>
 */
public final class NavigationGrid {

    /** Default tile size in world units. */
    public static final int DEFAULT_TILE_SIZE = 32;

    /** The size of each tile in world units. */
    private final int tileSize;

    /** Number of columns in the grid. */
    private final int gridWidth;

    /** Number of rows in the grid. */
    private final int gridHeight;

    /** Bit-per-cell storage where set bits indicate blocked cells. */
    private final BitSet blocked;

    /**
     * Creates a navigation grid with the specified world dimensions and tile size.
     *
     * @param width
     *            zone width in world units
     * @param height
     *            zone height in world units
     * @param tileSize
     *            the size of each tile in world units
     * @throws IllegalArgumentException
     *             if any argument is not positive
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
        this.tileSize = tileSize;
        this.gridWidth = (int) Math.ceil((double) width / tileSize);
        this.gridHeight = (int) Math.ceil((double) height / tileSize);
        this.blocked = new BitSet(gridWidth * gridHeight);
    }

    /**
     * Creates a navigation grid with the {@link #DEFAULT_TILE_SIZE}.
     *
     * @param width
     *            zone width in world units
     * @param height
     *            zone height in world units
     */
    public NavigationGrid(int width, int height) {
        this(width, height, DEFAULT_TILE_SIZE);
    }

    /** Converts world coordinates to grid indices. */
    public int[] worldToGrid(double x, double y) {
        return new int[]{(int) Math.floor(x / tileSize), (int) Math.floor(y / tileSize)};
    }

    /** Returns the center world coordinate of a grid cell. */
    public double[] gridToWorld(int gridX, int gridY) {
        return new double[]{(gridX + 0.5) * tileSize, (gridY + 0.5) * tileSize};
    }

    /** Returns {@code true} if the cell is blocked or out of bounds. */
    public boolean isBlocked(int gridX, int gridY) {
        return !isValidCell(gridX, gridY) || blocked.get(toIndex(gridX, gridY));
    }

    /** Returns {@code true} if the cell is walkable (in bounds and not blocked). */
    public boolean isWalkable(int gridX, int gridY) {
        return isValidCell(gridX, gridY) && !blocked.get(toIndex(gridX, gridY));
    }

    /** Marks a cell as blocked (no-op if out of bounds). */
    public void setBlocked(int gridX, int gridY) {
        if (isValidCell(gridX, gridY)) {
            blocked.set(toIndex(gridX, gridY));
        }
    }

    /** Marks a cell as walkable (no-op if out of bounds). */
    public void setWalkable(int gridX, int gridY) {
        if (isValidCell(gridX, gridY)) {
            blocked.clear(toIndex(gridX, gridY));
        }
    }

    /**
     * Marks all tiles within a rectangular area as blocked.
     *
     * @param worldX
     *            center X in world units
     * @param worldY
     *            center Y in world units
     * @param bbWidth
     *            bounding box width
     * @param bbHeight
     *            bounding box height
     */
    public void markAreaBlocked(double worldX, double worldY, double bbWidth, double bbHeight) {
        markArea(worldX, worldY, bbWidth, bbHeight, true);
    }

    /**
     * Marks all tiles within a rectangular area as walkable.
     *
     * @param worldX
     *            center X in world units
     * @param worldY
     *            center Y in world units
     * @param bbWidth
     *            bounding box width
     * @param bbHeight
     *            bounding box height
     */
    public void markAreaWalkable(double worldX, double worldY, double bbWidth, double bbHeight) {
        markArea(worldX, worldY, bbWidth, bbHeight, false);
    }

    /** Clears all blocked tiles, making the entire grid walkable. */
    public void clear() {
        blocked.clear();
    }

    /** Returns {@code true} if the cell coordinates are within bounds. */
    public boolean isValidCell(int gridX, int gridY) {
        return gridX >= 0 && gridX < gridWidth && gridY >= 0 && gridY < gridHeight;
    }

    public int getTileSize() {
        return tileSize;
    }

    public int getGridWidth() {
        return gridWidth;
    }

    public int getGridHeight() {
        return gridHeight;
    }

    /** Returns the number of blocked cells. */
    public int getBlockedCount() {
        return blocked.cardinality();
    }

    // ── internal ──

    private void markArea(double worldX, double worldY, double bbWidth, double bbHeight, boolean block) {
        double halfW = bbWidth / 2.0;
        double halfH = bbHeight / 2.0;
        int minGx = (int) Math.floor((worldX - halfW) / tileSize);
        int maxGx = (int) Math.floor((worldX + halfW) / tileSize);
        int minGy = (int) Math.floor((worldY - halfH) / tileSize);
        int maxGy = (int) Math.floor((worldY + halfH) / tileSize);
        for (int gx = minGx; gx <= maxGx; gx++) {
            for (int gy = minGy; gy <= maxGy; gy++) {
                if (block) {
                    setBlocked(gx, gy);
                } else {
                    setWalkable(gx, gy);
                }
            }
        }
    }

    private int toIndex(int gridX, int gridY) {
        return gridY * gridWidth + gridX;
    }
}
