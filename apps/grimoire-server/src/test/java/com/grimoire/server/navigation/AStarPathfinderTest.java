package com.grimoire.server.navigation;

import com.grimoire.server.component.Position;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AStarPathfinderTest {
    
    private NavigationGrid grid;
    
    @BeforeEach
    void setUp() {
        // Create a 320x320 world with 32px tiles = 10x10 grid
        grid = new NavigationGrid(320, 320, 32);
    }
    
    @Test
    void testPathOnEmptyGrid() {
        // Start at (16, 16) - cell (0,0), target at (272, 272) - cell (8,8)
        List<Position> path = AStarPathfinder.findPath(16, 16, 272, 272, grid);
        
        assertNotNull(path);
        assertFalse(path.isEmpty());
        
        // First waypoint should be at start cell center
        assertEquals(16.0, path.get(0).x(), 0.001);
        assertEquals(16.0, path.get(0).y(), 0.001);
        
        // Last waypoint should be at target cell center
        Position last = path.get(path.size() - 1);
        assertEquals(272.0, last.x(), 0.001);
        assertEquals(272.0, last.y(), 0.001);
    }
    
    @Test
    void testSameStartAndTarget() {
        List<Position> path = AStarPathfinder.findPath(50, 50, 55, 55, grid);
        
        // Both are in cell (1,1), so path should be empty
        assertNotNull(path);
        assertTrue(path.isEmpty());
    }
    
    @Test
    void testPathToBlockedTarget() {
        grid.setBlocked(5, 5);
        
        // Target is in blocked cell
        List<Position> path = AStarPathfinder.findPath(16, 16, 176, 176, grid);
        
        assertNull(path, "Should return null when target is blocked");
    }
    
    @Test
    void testPathFromBlockedStart() {
        grid.setBlocked(0, 0);
        
        // Start is in blocked cell
        List<Position> path = AStarPathfinder.findPath(16, 16, 176, 176, grid);
        
        assertNull(path, "Should return null when start is blocked");
    }
    
    @Test
    void testPathAroundObstacle() {
        // Create a wall blocking direct path from (0,0) to (4,0)
        grid.setBlocked(2, 0);
        grid.setBlocked(2, 1);
        grid.setBlocked(2, 2);
        
        // Path from left to right
        List<Position> path = AStarPathfinder.findPath(16, 16, 144, 16, grid);
        
        assertNotNull(path, "Path should exist around obstacle");
        assertTrue(path.size() > 2, "Path should go around the wall");
        
        // Verify the path doesn't go through blocked cells
        for (Position pos : path) {
            int[] gridCoords = grid.worldToGrid(pos.x(), pos.y());
            assertFalse(grid.isBlocked(gridCoords[0], gridCoords[1]),
                    "Path should not go through blocked cell at (" + gridCoords[0] + ", " + gridCoords[1] + ")");
        }
    }
    
    @Test
    void testNoPathWhenCompletelyBlocked() {
        // Block a complete row, splitting the grid
        for (int x = 0; x < 10; x++) {
            grid.setBlocked(x, 5);
        }
        
        // Try to path from top to bottom
        List<Position> path = AStarPathfinder.findPath(16, 16, 16, 272, grid);
        
        assertNull(path, "Should return null when no path exists");
    }
    
    @Test
    void testNullGrid() {
        List<Position> path = AStarPathfinder.findPath(0, 0, 100, 100, null);
        
        assertNull(path);
    }
    
    @Test
    void testDiagonalMovement() {
        // Create obstacles that force diagonal movement to be optimal
        // Path from (0,0) to (3,3) should ideally be diagonal
        List<Position> path = AStarPathfinder.findPath(16, 16, 112, 112, grid);
        
        assertNotNull(path);
        // Diagonal path should be shorter than manhattan distance
        // Direct diagonal would be 4 cells, manhattan would be 6
        assertTrue(path.size() <= 5, "Diagonal path should be efficient");
    }
    
    @Test
    void testPathWithNarrowPassage() {
        // Create a narrow passage by blocking all but one cell in a row
        for (int x = 0; x < 10; x++) {
            if (x != 4) {
                grid.setBlocked(x, 5);
            }
        }
        
        // Path must go through the gap at (4, 5)
        List<Position> path = AStarPathfinder.findPath(16, 16, 16, 272, grid);
        
        assertNotNull(path, "Path should exist through narrow passage");
        
        // Verify path goes through the gap
        boolean passedThroughGap = false;
        for (Position pos : path) {
            int[] coords = grid.worldToGrid(pos.x(), pos.y());
            if (coords[0] == 4 && coords[1] == 5) {
                passedThroughGap = true;
                break;
            }
        }
        assertTrue(passedThroughGap, "Path should go through the narrow passage");
    }
    
    @Test
    void testPathSmoothing() {
        // Create a simple path that could be smoothed
        List<Position> path = AStarPathfinder.findPath(16, 16, 144, 144, grid);
        
        assertNotNull(path);
        
        List<Position> smoothed = AStarPathfinder.smoothPath(path, grid);
        
        assertNotNull(smoothed);
        assertTrue(smoothed.size() <= path.size(), "Smoothed path should not be longer");
        
        // First and last points should be preserved
        assertEquals(path.get(0).x(), smoothed.get(0).x(), 0.001);
        assertEquals(path.get(0).y(), smoothed.get(0).y(), 0.001);
        assertEquals(path.get(path.size() - 1).x(), smoothed.get(smoothed.size() - 1).x(), 0.001);
        assertEquals(path.get(path.size() - 1).y(), smoothed.get(smoothed.size() - 1).y(), 0.001);
    }
    
    @Test
    void testSmoothPathNull() {
        assertNull(AStarPathfinder.smoothPath(null, grid));
    }
    
    @Test
    void testSmoothPathNullGrid() {
        List<Position> path = AStarPathfinder.findPath(16, 16, 144, 144, grid);
        List<Position> smoothed = AStarPathfinder.smoothPath(path, null);
        assertEquals(path, smoothed, "Should return original path when grid is null");
    }
    
    @Test
    void testSmoothPathShortPath() {
        // Path with only 2 points shouldn't be modified
        List<Position> shortPath = List.of(new Position(16, 16), new Position(48, 48));
        List<Position> smoothed = AStarPathfinder.smoothPath(shortPath, grid);
        
        assertEquals(2, smoothed.size());
    }
    
    @Test
    void testHasLineOfSightClear() {
        Position from = new Position(16, 16);
        Position to = new Position(144, 144);
        
        assertTrue(AStarPathfinder.hasLineOfSight(from, to, grid));
    }
    
    @Test
    void testHasLineOfSightBlocked() {
        grid.setBlocked(2, 2);
        
        Position from = new Position(16, 16);
        Position to = new Position(144, 144);
        
        assertFalse(AStarPathfinder.hasLineOfSight(from, to, grid));
    }
    
    @Test
    void testPathDoesNotCutCorners() {
        // Create an L-shaped obstacle
        grid.setBlocked(3, 3);
        grid.setBlocked(3, 4);
        grid.setBlocked(4, 3);
        
        // Path that might try to cut the corner
        List<Position> path = AStarPathfinder.findPath(80, 80, 144, 144, grid);
        
        assertNotNull(path);
        
        // The path should go around, not through the diagonal
        for (Position pos : path) {
            int[] coords = grid.worldToGrid(pos.x(), pos.y());
            assertFalse(grid.isBlocked(coords[0], coords[1]));
        }
    }
    
    @Test
    void testAdjacentCellPath() {
        // Path to an immediately adjacent cell
        List<Position> path = AStarPathfinder.findPath(16, 16, 48, 16, grid);
        
        assertNotNull(path);
        assertEquals(2, path.size(), "Path to adjacent cell should have 2 waypoints");
    }
    
    @Test
    void testLongPath() {
        // Create a larger grid for this test
        NavigationGrid largeGrid = new NavigationGrid(1024, 1024, 32);
        
        // Path across the entire grid
        List<Position> path = AStarPathfinder.findPath(16, 16, 1008, 1008, largeGrid);
        
        assertNotNull(path, "Should find path on large grid");
        assertFalse(path.isEmpty());
    }
    
    @Test
    void testMazeNavigation() {
        // Create a simple maze pattern
        // The grid is 10x10 cells (320/32)
        // We'll create a simple wall with a gap
        
        // Create vertical wall from (5,1) to (5,7) with a gap at (5,4)
        for (int y = 1; y <= 7; y++) {
            if (y != 4) {
                grid.setBlocked(5, y);
            }
        }
        
        // Path from left side to right side must go through the gap
        List<Position> path = AStarPathfinder.findPath(48, 112, 208, 112, grid);
        
        assertNotNull(path, "Should find path through maze gap");
        
        // Verify all waypoints are walkable
        for (Position pos : path) {
            int[] coords = grid.worldToGrid(pos.x(), pos.y());
            assertFalse(grid.isBlocked(coords[0], coords[1]), 
                    "Path should not include blocked cells: (" + coords[0] + ", " + coords[1] + ")");
        }
        
        // Path must go through the gap cell (5,4)
        boolean passedThroughGap = false;
        for (Position pos : path) {
            int[] coords = grid.worldToGrid(pos.x(), pos.y());
            if (coords[0] == 5 && coords[1] == 4) {
                passedThroughGap = true;
                break;
            }
        }
        assertTrue(passedThroughGap, "Path should go through the gap at (5,4)");
    }
}
