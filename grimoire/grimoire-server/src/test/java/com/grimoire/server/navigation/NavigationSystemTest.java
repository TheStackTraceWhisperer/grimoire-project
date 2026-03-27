package com.grimoire.server.navigation;

import com.grimoire.server.component.*;
import com.grimoire.server.config.GameConfig;
import com.grimoire.server.config.TestGameConfig;
import com.grimoire.server.ecs.ComponentManager;
import com.grimoire.server.ecs.EcsWorld;
import com.grimoire.server.ecs.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NavigationSystemTest {
    
    private EcsWorld ecsWorld;
    private NavigationSystem navigationSystem;
    
    @BeforeEach
    void setUp() {
        EntityManager entityManager = new EntityManager();
        ComponentManager componentManager = new ComponentManager();
        ecsWorld = new EcsWorld(entityManager, componentManager);
        GameConfig gameConfig = TestGameConfig.create();
        navigationSystem = new NavigationSystem(ecsWorld, gameConfig);
    }
    
    @Test
    void testGridCreatedOnDemand() {
        NavigationGrid grid = navigationSystem.getOrCreateGrid("zone1");
        
        assertNotNull(grid);
        assertEquals(grid, navigationSystem.getGrid("zone1"));
    }
    
    @Test
    void testGetGridReturnsNullIfNotCreated() {
        assertNull(navigationSystem.getGrid("nonexistent"));
    }
    
    @Test
    void testRebuildAllGridsMarksBlockedAreas() {
        // Create a solid entity with bounding box
        String wallId = ecsWorld.createEntity();
        ecsWorld.addComponent(wallId, new Solid());
        ecsWorld.addComponent(wallId, new Position(100, 100));
        ecsWorld.addComponent(wallId, new BoundingBox(32, 32));
        ecsWorld.addComponent(wallId, new Zone("zone1"));
        
        // Force grid creation and rebuild
        navigationSystem.getOrCreateGrid("zone1");
        navigationSystem.rebuildAllGrids();
        
        NavigationGrid grid = navigationSystem.getGrid("zone1");
        
        // The entity at (100, 100) with 32x32 box should block nearby cells
        int[] gridCoords = grid.worldToGrid(100, 100);
        assertTrue(grid.isBlocked(gridCoords[0], gridCoords[1]), 
                "Cell at entity center should be blocked");
    }
    
    @Test
    void testRebuildAllGridsClearsExistingData() {
        // Create initial solid entity
        String wallId = ecsWorld.createEntity();
        ecsWorld.addComponent(wallId, new Solid());
        ecsWorld.addComponent(wallId, new Position(100, 100));
        ecsWorld.addComponent(wallId, new BoundingBox(32, 32));
        ecsWorld.addComponent(wallId, new Zone("zone1"));
        
        navigationSystem.getOrCreateGrid("zone1");
        navigationSystem.rebuildAllGrids();
        
        NavigationGrid grid = navigationSystem.getGrid("zone1");
        int initialBlocked = grid.getBlockedCount();
        assertTrue(initialBlocked > 0);
        
        // Remove the entity
        ecsWorld.destroyEntity(wallId);
        
        // Rebuild should clear the blocked area
        navigationSystem.rebuildAllGrids();
        
        assertEquals(0, grid.getBlockedCount(), "Grid should be empty after entity removed");
    }
    
    @Test
    void testEntitiesWithoutBoundingBoxIgnored() {
        String wallId = ecsWorld.createEntity();
        ecsWorld.addComponent(wallId, new Solid());
        ecsWorld.addComponent(wallId, new Position(100, 100));
        // No BoundingBox
        ecsWorld.addComponent(wallId, new Zone("zone1"));
        
        navigationSystem.getOrCreateGrid("zone1");
        navigationSystem.rebuildAllGrids();
        
        NavigationGrid grid = navigationSystem.getGrid("zone1");
        assertEquals(0, grid.getBlockedCount(), "Entity without BoundingBox should not block");
    }
    
    @Test
    void testEntitiesWithoutPositionIgnored() {
        String wallId = ecsWorld.createEntity();
        ecsWorld.addComponent(wallId, new Solid());
        // No Position
        ecsWorld.addComponent(wallId, new BoundingBox(32, 32));
        ecsWorld.addComponent(wallId, new Zone("zone1"));
        
        navigationSystem.getOrCreateGrid("zone1");
        navigationSystem.rebuildAllGrids();
        
        NavigationGrid grid = navigationSystem.getGrid("zone1");
        assertEquals(0, grid.getBlockedCount(), "Entity without Position should not block");
    }
    
    @Test
    void testDefaultZoneUsedWhenNoZoneComponent() {
        String wallId = ecsWorld.createEntity();
        ecsWorld.addComponent(wallId, new Solid());
        ecsWorld.addComponent(wallId, new Position(100, 100));
        ecsWorld.addComponent(wallId, new BoundingBox(32, 32));
        // No Zone component - should use "default"
        
        navigationSystem.getOrCreateGrid("default");
        navigationSystem.rebuildAllGrids();
        
        NavigationGrid grid = navigationSystem.getGrid("default");
        assertTrue(grid.getBlockedCount() > 0, "Entity should be placed in default zone");
    }
    
    @Test
    void testMultipleZones() {
        // Entity in zone1
        String wall1 = ecsWorld.createEntity();
        ecsWorld.addComponent(wall1, new Solid());
        ecsWorld.addComponent(wall1, new Position(100, 100));
        ecsWorld.addComponent(wall1, new BoundingBox(32, 32));
        ecsWorld.addComponent(wall1, new Zone("zone1"));
        
        // Entity in zone2
        String wall2 = ecsWorld.createEntity();
        ecsWorld.addComponent(wall2, new Solid());
        ecsWorld.addComponent(wall2, new Position(200, 200));
        ecsWorld.addComponent(wall2, new BoundingBox(64, 64));
        ecsWorld.addComponent(wall2, new Zone("zone2"));
        
        navigationSystem.getOrCreateGrid("zone1");
        navigationSystem.getOrCreateGrid("zone2");
        navigationSystem.rebuildAllGrids();
        
        NavigationGrid grid1 = navigationSystem.getGrid("zone1");
        NavigationGrid grid2 = navigationSystem.getGrid("zone2");
        
        // Zone1 should have blocked cells from wall1
        int[] coords1 = grid1.worldToGrid(100, 100);
        assertTrue(grid1.isBlocked(coords1[0], coords1[1]));
        
        // Zone2 should have blocked cells from wall2 (which is larger)
        int[] coords2 = grid2.worldToGrid(200, 200);
        assertTrue(grid2.isBlocked(coords2[0], coords2[1]));
        
        // Ensure zones are independent
        int[] coords1InGrid2 = grid2.worldToGrid(100, 100);
        assertTrue(grid2.isWalkable(coords1InGrid2[0], coords1InGrid2[1]), 
                "Zone2 should not have zone1's wall blocked");
    }
    
    @Test
    void testRebuildSpecificZone() {
        // Entity in zone1
        String wall1 = ecsWorld.createEntity();
        ecsWorld.addComponent(wall1, new Solid());
        ecsWorld.addComponent(wall1, new Position(100, 100));
        ecsWorld.addComponent(wall1, new BoundingBox(32, 32));
        ecsWorld.addComponent(wall1, new Zone("zone1"));
        
        navigationSystem.getOrCreateGrid("zone1");
        navigationSystem.rebuildGrid("zone1");
        
        NavigationGrid grid = navigationSystem.getGrid("zone1");
        assertTrue(grid.getBlockedCount() > 0);
    }
    
    @Test
    void testRemoveGrid() {
        navigationSystem.getOrCreateGrid("zone1");
        assertEquals(1, navigationSystem.getGridCount());
        
        navigationSystem.removeGrid("zone1");
        
        assertEquals(0, navigationSystem.getGridCount());
        assertNull(navigationSystem.getGrid("zone1"));
    }
    
    @Test
    void testTickUpdatesPeriodically() {
        String wallId = ecsWorld.createEntity();
        ecsWorld.addComponent(wallId, new Solid());
        ecsWorld.addComponent(wallId, new Position(100, 100));
        ecsWorld.addComponent(wallId, new BoundingBox(32, 32));
        ecsWorld.addComponent(wallId, new Zone("zone1"));
        
        navigationSystem.getOrCreateGrid("zone1");
        
        // First tick shouldn't trigger update (counter = 1)
        navigationSystem.tick(0.05f);
        
        NavigationGrid grid = navigationSystem.getGrid("zone1");
        
        // Run enough ticks to trigger an update (every 10 ticks)
        for (int i = 0; i < 9; i++) {
            navigationSystem.tick(0.05f);
        }
        
        // After 10 ticks, grid should be updated
        assertTrue(grid.getBlockedCount() > 0, "Grid should be updated after 10 ticks");
    }
    
    @Test
    void testLargeBoundingBoxBlocksMultipleCells() {
        // Create a large solid obstacle (128x128)
        String wallId = ecsWorld.createEntity();
        ecsWorld.addComponent(wallId, new Solid());
        ecsWorld.addComponent(wallId, new Position(128, 128));
        ecsWorld.addComponent(wallId, new BoundingBox(128, 128));
        ecsWorld.addComponent(wallId, new Zone("zone1"));
        
        navigationSystem.getOrCreateGrid("zone1");
        navigationSystem.rebuildAllGrids();
        
        NavigationGrid grid = navigationSystem.getGrid("zone1");
        
        // Should block many cells
        assertTrue(grid.getBlockedCount() > 1, 
                "Large obstacle should block multiple cells: " + grid.getBlockedCount());
    }
    
    @Test
    void testDirtyZoneTracking() {
        navigationSystem.getOrCreateGrid("zone1");
        navigationSystem.rebuildAllGrids();
        
        // Initially no dirty zones after rebuild
        assertEquals(0, navigationSystem.getDirtyZoneCount());
        
        // Mark a zone as dirty
        navigationSystem.markZoneDirty("zone1");
        assertEquals(1, navigationSystem.getDirtyZoneCount());
    }
    
    @Test
    void testOnlyDirtyZonesAreRebuilt() {
        // Create entity in zone1
        String wall1 = ecsWorld.createEntity();
        ecsWorld.addComponent(wall1, new Solid());
        ecsWorld.addComponent(wall1, new Position(100, 100));
        ecsWorld.addComponent(wall1, new BoundingBox(32, 32));
        ecsWorld.addComponent(wall1, new Zone("zone1"));
        
        navigationSystem.getOrCreateGrid("zone1");
        navigationSystem.getOrCreateGrid("zone2");
        navigationSystem.rebuildAllGrids();
        
        NavigationGrid grid1 = navigationSystem.getGrid("zone1");
        NavigationGrid grid2 = navigationSystem.getGrid("zone2");
        
        int initialBlocked1 = grid1.getBlockedCount();
        int initialBlocked2 = grid2.getBlockedCount();
        
        // Add entity to zone2
        String wall2 = ecsWorld.createEntity();
        ecsWorld.addComponent(wall2, new Solid());
        ecsWorld.addComponent(wall2, new Position(200, 200));
        ecsWorld.addComponent(wall2, new BoundingBox(32, 32));
        ecsWorld.addComponent(wall2, new Zone("zone2"));
        
        // Trigger tick-based update (needs 10 ticks)
        for (int i = 0; i < 10; i++) {
            navigationSystem.tick(0.05f);
        }
        
        // Zone2 should have more blocked cells now
        assertTrue(grid2.getBlockedCount() > initialBlocked2, 
                "Zone2 should have been rebuilt with new entity");
        
        // Zone1 should still have the same blocked count (unchanged)
        assertEquals(initialBlocked1, grid1.getBlockedCount(),
                "Zone1 should not have changed");
    }
    
    @Test
    void testEntityRemovalMarksDirty() {
        // Create entity in zone1
        String wall1 = ecsWorld.createEntity();
        ecsWorld.addComponent(wall1, new Solid());
        ecsWorld.addComponent(wall1, new Position(100, 100));
        ecsWorld.addComponent(wall1, new BoundingBox(32, 32));
        ecsWorld.addComponent(wall1, new Zone("zone1"));
        
        navigationSystem.getOrCreateGrid("zone1");
        navigationSystem.rebuildAllGrids();
        
        NavigationGrid grid = navigationSystem.getGrid("zone1");
        int initialBlocked = grid.getBlockedCount();
        assertTrue(initialBlocked > 0);
        
        // Remove the entity
        ecsWorld.destroyEntity(wall1);
        
        // Trigger tick-based update
        for (int i = 0; i < 10; i++) {
            navigationSystem.tick(0.05f);
        }
        
        // Grid should now be empty (entity removed)
        assertEquals(0, grid.getBlockedCount(), 
                "Grid should be empty after entity removed");
    }
}
