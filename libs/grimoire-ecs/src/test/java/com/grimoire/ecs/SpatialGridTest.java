package com.grimoire.ecs;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class SpatialGridTest {
    
    private SpatialGrid grid;
    
    @BeforeEach
    void setUp() {
        grid = new SpatialGrid(64); // 64 unit cell size
    }
    
    @Test
    void testAddEntityToGrid() {
        grid.updateEntity("entity1", 100, 100, "zone1");
        
        assertEquals(1, grid.getEntityCount());
        assertEquals(1, grid.getCellCount());
    }
    
    @Test
    void testGetNearbyEntitiesSameCell() {
        grid.updateEntity("entity1", 100, 100, "zone1");
        grid.updateEntity("entity2", 110, 110, "zone1"); // Same cell as entity1
        
        Set<String> nearby = grid.getNearbyEntities(105, 105, "zone1");
        
        assertTrue(nearby.contains("entity1"));
        assertTrue(nearby.contains("entity2"));
        assertEquals(2, nearby.size());
    }
    
    @Test
    void testGetNearbyEntitiesAdjacentCell() {
        grid.updateEntity("entity1", 30, 30, "zone1");  // Cell (0,0)
        grid.updateEntity("entity2", 70, 70, "zone1");  // Cell (1,1)
        
        // Query at (40, 40) which is in cell (0,0), but entity2 is in adjacent cell (1,1)
        Set<String> nearby = grid.getNearbyEntities(40, 40, "zone1");
        
        assertTrue(nearby.contains("entity1"));
        assertTrue(nearby.contains("entity2"));
    }
    
    @Test
    void testGetNearbyEntitiesFarCell() {
        grid.updateEntity("entity1", 0, 0, "zone1");       // Cell (0,0)
        grid.updateEntity("entity2", 200, 200, "zone1");   // Cell (3,3) - far away
        
        Set<String> nearby = grid.getNearbyEntities(0, 0, "zone1");
        
        assertTrue(nearby.contains("entity1"));
        assertFalse(nearby.contains("entity2")); // Too far to be in nearby cells
    }
    
    @Test
    void testGetNearbyEntitiesDifferentZone() {
        grid.updateEntity("entity1", 100, 100, "zone1");
        grid.updateEntity("entity2", 100, 100, "zone2"); // Same position, different zone
        
        Set<String> nearbyZone1 = grid.getNearbyEntities(100, 100, "zone1");
        Set<String> nearbyZone2 = grid.getNearbyEntities(100, 100, "zone2");
        
        assertTrue(nearbyZone1.contains("entity1"));
        assertFalse(nearbyZone1.contains("entity2"));
        
        assertTrue(nearbyZone2.contains("entity2"));
        assertFalse(nearbyZone2.contains("entity1"));
    }
    
    @Test
    void testRemoveEntity() {
        grid.updateEntity("entity1", 100, 100, "zone1");
        grid.removeEntity("entity1");
        
        assertEquals(0, grid.getEntityCount());
        
        Set<String> nearby = grid.getNearbyEntities(100, 100, "zone1");
        assertTrue(nearby.isEmpty());
    }
    
    @Test
    void testUpdateEntityPosition() {
        grid.updateEntity("entity1", 0, 0, "zone1");
        
        Set<String> nearbyOrigin = grid.getNearbyEntities(0, 0, "zone1");
        assertTrue(nearbyOrigin.contains("entity1"));
        
        // Move to a far cell
        grid.updateEntity("entity1", 200, 200, "zone1");
        
        // Should no longer be near origin
        nearbyOrigin = grid.getNearbyEntities(0, 0, "zone1");
        assertFalse(nearbyOrigin.contains("entity1"));
        
        // Should be near new position
        Set<String> nearbyNew = grid.getNearbyEntities(200, 200, "zone1");
        assertTrue(nearbyNew.contains("entity1"));
    }
    
    @Test
    void testUpdateEntitySameCell() {
        grid.updateEntity("entity1", 10, 10, "zone1");
        grid.updateEntity("entity1", 20, 20, "zone1"); // Same cell
        
        assertEquals(1, grid.getEntityCount());
        assertEquals(1, grid.getCellCount());
    }
    
    @Test
    void testClear() {
        grid.updateEntity("entity1", 100, 100, "zone1");
        grid.updateEntity("entity2", 200, 200, "zone1");
        
        grid.clear();
        
        assertEquals(0, grid.getEntityCount());
        assertEquals(0, grid.getCellCount());
    }
    
    @Test
    void testInvalidCellSize() {
        assertThrows(IllegalArgumentException.class, () -> new SpatialGrid(0));
        assertThrows(IllegalArgumentException.class, () -> new SpatialGrid(-1));
    }
    
    @Test
    void testNegativeCoordinates() {
        grid.updateEntity("entity1", -100, -100, "zone1");
        
        Set<String> nearby = grid.getNearbyEntities(-100, -100, "zone1");
        assertTrue(nearby.contains("entity1"));
    }
}
