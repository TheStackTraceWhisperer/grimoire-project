package com.grimoire.server.navigation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NavigationGridTest {
    
    @Test
    void testConstructorWithValidDimensions() {
        NavigationGrid grid = new NavigationGrid(640, 480, 32);
        
        assertEquals(32, grid.getTileSize());
        assertEquals(20, grid.getGridWidth());
        assertEquals(15, grid.getGridHeight());
    }
    
    @Test
    void testConstructorWithDefaultTileSize() {
        NavigationGrid grid = new NavigationGrid(640, 480);
        
        assertEquals(NavigationGrid.DEFAULT_TILE_SIZE, grid.getTileSize());
        assertEquals(20, grid.getGridWidth());
        assertEquals(15, grid.getGridHeight());
    }
    
    @Test
    void testConstructorWithNonEvenDimensions() {
        // 100 / 32 = 3.125, should ceil to 4
        NavigationGrid grid = new NavigationGrid(100, 100, 32);
        
        assertEquals(4, grid.getGridWidth());
        assertEquals(4, grid.getGridHeight());
    }
    
    @Test
    void testConstructorWithInvalidWidth() {
        assertThrows(IllegalArgumentException.class, () -> new NavigationGrid(0, 480, 32));
        assertThrows(IllegalArgumentException.class, () -> new NavigationGrid(-100, 480, 32));
    }
    
    @Test
    void testConstructorWithInvalidHeight() {
        assertThrows(IllegalArgumentException.class, () -> new NavigationGrid(640, 0, 32));
        assertThrows(IllegalArgumentException.class, () -> new NavigationGrid(640, -100, 32));
    }
    
    @Test
    void testConstructorWithInvalidTileSize() {
        assertThrows(IllegalArgumentException.class, () -> new NavigationGrid(640, 480, 0));
        assertThrows(IllegalArgumentException.class, () -> new NavigationGrid(640, 480, -32));
    }
    
    @Test
    void testWorldToGridConversion() {
        NavigationGrid grid = new NavigationGrid(640, 480, 32);
        
        // Test origin
        int[] result = grid.worldToGrid(0, 0);
        assertEquals(0, result[0]);
        assertEquals(0, result[1]);
        
        // Test middle of first cell
        result = grid.worldToGrid(16, 16);
        assertEquals(0, result[0]);
        assertEquals(0, result[1]);
        
        // Test at cell boundary
        result = grid.worldToGrid(32, 32);
        assertEquals(1, result[0]);
        assertEquals(1, result[1]);
        
        // Test arbitrary position
        result = grid.worldToGrid(100, 200);
        assertEquals(3, result[0]); // 100 / 32 = 3.125 -> 3
        assertEquals(6, result[1]); // 200 / 32 = 6.25 -> 6
    }
    
    @Test
    void testGridToWorldConversion() {
        NavigationGrid grid = new NavigationGrid(640, 480, 32);
        
        // Test origin cell center
        double[] result = grid.gridToWorld(0, 0);
        assertEquals(16.0, result[0], 0.001); // 0.5 * 32 = 16
        assertEquals(16.0, result[1], 0.001);
        
        // Test cell (1, 1) center
        result = grid.gridToWorld(1, 1);
        assertEquals(48.0, result[0], 0.001); // 1.5 * 32 = 48
        assertEquals(48.0, result[1], 0.001);
        
        // Test arbitrary cell
        result = grid.gridToWorld(5, 10);
        assertEquals(176.0, result[0], 0.001); // 5.5 * 32 = 176
        assertEquals(336.0, result[1], 0.001); // 10.5 * 32 = 336
    }
    
    @Test
    void testRoundTripConversion() {
        NavigationGrid grid = new NavigationGrid(640, 480, 32);
        
        // Start with grid coordinates, convert to world, then back to grid
        for (int gx = 0; gx < 5; gx++) {
            for (int gy = 0; gy < 5; gy++) {
                double[] worldCoords = grid.gridToWorld(gx, gy);
                int[] gridCoords = grid.worldToGrid(worldCoords[0], worldCoords[1]);
                assertEquals(gx, gridCoords[0], "X coordinate mismatch for (" + gx + ", " + gy + ")");
                assertEquals(gy, gridCoords[1], "Y coordinate mismatch for (" + gx + ", " + gy + ")");
            }
        }
    }
    
    @Test
    void testNewGridIsWalkable() {
        NavigationGrid grid = new NavigationGrid(640, 480, 32);
        
        // All cells should start as walkable
        for (int x = 0; x < grid.getGridWidth(); x++) {
            for (int y = 0; y < grid.getGridHeight(); y++) {
                assertTrue(grid.isWalkable(x, y), "Cell (" + x + ", " + y + ") should be walkable");
                assertFalse(grid.isBlocked(x, y), "Cell (" + x + ", " + y + ") should not be blocked");
            }
        }
        assertEquals(0, grid.getBlockedCount());
    }
    
    @Test
    void testSetBlocked() {
        NavigationGrid grid = new NavigationGrid(640, 480, 32);
        
        grid.setBlocked(5, 10);
        
        assertTrue(grid.isBlocked(5, 10));
        assertFalse(grid.isWalkable(5, 10));
        assertEquals(1, grid.getBlockedCount());
    }
    
    @Test
    void testSetWalkable() {
        NavigationGrid grid = new NavigationGrid(640, 480, 32);
        
        grid.setBlocked(5, 10);
        assertTrue(grid.isBlocked(5, 10));
        
        grid.setWalkable(5, 10);
        
        assertFalse(grid.isBlocked(5, 10));
        assertTrue(grid.isWalkable(5, 10));
        assertEquals(0, grid.getBlockedCount());
    }
    
    @Test
    void testOutOfBoundsIsBlocked() {
        NavigationGrid grid = new NavigationGrid(640, 480, 32);
        
        // Negative coordinates
        assertTrue(grid.isBlocked(-1, 0));
        assertTrue(grid.isBlocked(0, -1));
        assertFalse(grid.isWalkable(-1, 0));
        assertFalse(grid.isWalkable(0, -1));
        
        // Beyond grid bounds
        assertTrue(grid.isBlocked(grid.getGridWidth(), 0));
        assertTrue(grid.isBlocked(0, grid.getGridHeight()));
        assertFalse(grid.isWalkable(grid.getGridWidth(), 0));
        assertFalse(grid.isWalkable(0, grid.getGridHeight()));
    }
    
    @Test
    void testSetBlockedOutOfBoundsIgnored() {
        NavigationGrid grid = new NavigationGrid(640, 480, 32);
        
        // Should not throw
        grid.setBlocked(-1, 0);
        grid.setBlocked(0, -1);
        grid.setBlocked(grid.getGridWidth(), 0);
        grid.setBlocked(0, grid.getGridHeight());
        
        assertEquals(0, grid.getBlockedCount());
    }
    
    @Test
    void testMarkAreaBlocked() {
        NavigationGrid grid = new NavigationGrid(640, 480, 32);
        
        // Block a 64x64 area centered at (64, 64)
        // halfWidth = 32, halfHeight = 32
        // minX = 64-32=32, maxX = 64+32=96 -> floor(32/32)=1, floor(96/32)=3
        // minY = 64-32=32, maxY = 64+32=96 -> floor(32/32)=1, floor(96/32)=3
        // Cells: (1,1) to (3,3) = 9 cells total
        grid.markAreaBlocked(64, 64, 64, 64);
        
        // All cells from (1,1) to (3,3) should be blocked
        for (int x = 1; x <= 3; x++) {
            for (int y = 1; y <= 3; y++) {
                assertTrue(grid.isBlocked(x, y), "Cell (" + x + ", " + y + ") should be blocked");
            }
        }
        assertEquals(9, grid.getBlockedCount());
        
        // Cells outside area should be walkable
        assertTrue(grid.isWalkable(0, 0));
        assertTrue(grid.isWalkable(4, 4));
    }
    
    @Test
    void testMarkAreaWalkable() {
        NavigationGrid grid = new NavigationGrid(640, 480, 32);
        
        // First block cells from (1,1) to (3,3)
        for (int x = 1; x <= 3; x++) {
            for (int y = 1; y <= 3; y++) {
                grid.setBlocked(x, y);
            }
        }
        assertEquals(9, grid.getBlockedCount());
        
        // Clear the area with a 64x64 box centered at (64,64)
        grid.markAreaWalkable(64, 64, 64, 64);
        
        for (int x = 1; x <= 3; x++) {
            for (int y = 1; y <= 3; y++) {
                assertTrue(grid.isWalkable(x, y), "Cell (" + x + ", " + y + ") should be walkable");
            }
        }
        assertEquals(0, grid.getBlockedCount());
    }
    
    @Test
    void testClear() {
        NavigationGrid grid = new NavigationGrid(640, 480, 32);
        
        // Block several cells
        grid.setBlocked(0, 0);
        grid.setBlocked(5, 5);
        grid.setBlocked(10, 10);
        assertEquals(3, grid.getBlockedCount());
        
        grid.clear();
        
        assertEquals(0, grid.getBlockedCount());
        assertTrue(grid.isWalkable(0, 0));
        assertTrue(grid.isWalkable(5, 5));
        assertTrue(grid.isWalkable(10, 10));
    }
    
    @Test
    void testIsValidCell() {
        NavigationGrid grid = new NavigationGrid(320, 240, 32);
        
        // Valid cells
        assertTrue(grid.isValidCell(0, 0));
        assertTrue(grid.isValidCell(9, 7)); // Last valid cell (10x8 grid - 1)
        assertTrue(grid.isValidCell(5, 3));
        
        // Invalid cells
        assertFalse(grid.isValidCell(-1, 0));
        assertFalse(grid.isValidCell(0, -1));
        assertFalse(grid.isValidCell(10, 0)); // Grid is 10 cells wide (0-9)
        assertFalse(grid.isValidCell(0, 8));  // Grid is 8 cells tall (0-7)
    }
    
    @Test
    void testMarkSmallBoundingBox() {
        NavigationGrid grid = new NavigationGrid(640, 480, 32);
        
        // Block a small entity (16x16) at position (50, 50)
        // This should only block cell (1, 1) since 50/32 = 1
        grid.markAreaBlocked(50, 50, 16, 16);
        
        assertTrue(grid.isBlocked(1, 1));
        assertEquals(1, grid.getBlockedCount());
    }
    
    @Test
    void testMarkLargeBoundingBox() {
        NavigationGrid grid = new NavigationGrid(640, 480, 32);
        
        // Block a large area spanning multiple cells
        // 128x128 centered at (128, 128)
        // halfWidth=64, halfHeight=64
        // minX=64, maxX=192 -> floor(64/32)=2, floor(192/32)=6
        // minY=64, maxY=192 -> floor(64/32)=2, floor(192/32)=6
        // Grid cells: (2,2) to (6,6) = 5x5 = 25 cells
        grid.markAreaBlocked(128, 128, 128, 128);
        
        for (int x = 2; x <= 6; x++) {
            for (int y = 2; y <= 6; y++) {
                assertTrue(grid.isBlocked(x, y), "Cell (" + x + ", " + y + ") should be blocked");
            }
        }
        assertEquals(25, grid.getBlockedCount());
    }
    
    @Test
    void testNegativeWorldCoordinates() {
        NavigationGrid grid = new NavigationGrid(640, 480, 32);
        
        // Negative coordinates should map to negative grid indices
        int[] result = grid.worldToGrid(-16, -16);
        assertEquals(-1, result[0]);
        assertEquals(-1, result[1]);
        
        // Which should be treated as blocked/out of bounds
        assertTrue(grid.isBlocked(result[0], result[1]));
    }
}
