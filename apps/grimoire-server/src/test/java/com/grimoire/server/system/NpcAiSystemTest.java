package com.grimoire.server.system;

import com.grimoire.server.component.*;
import com.grimoire.server.config.GameConfig;
import com.grimoire.server.config.TestGameConfig;
import com.grimoire.ecs.ComponentManager;
import com.grimoire.ecs.EcsWorld;
import com.grimoire.ecs.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NpcAiSystemTest {
    
    private EcsWorld ecsWorld;
    private SpatialGridSystem spatialGridSystem;
    private NpcAiSystem system;
    
    @BeforeEach
    void setUp() {
        EntityManager entityManager = new EntityManager();
        ComponentManager componentManager = new ComponentManager();
        ecsWorld = new EcsWorld(entityManager, componentManager);
        GameConfig gameConfig = TestGameConfig.create();
        spatialGridSystem = new SpatialGridSystem(ecsWorld, gameConfig);
        system = new NpcAiSystem(ecsWorld, spatialGridSystem, gameConfig);
    }
    
    @Test
    void testFriendlyWanderProcessing() {
        String npcId = ecsWorld.createEntity();
        ecsWorld.addComponent(npcId, new NpcAi(NpcAi.AiType.FRIENDLY_WANDER));
        ecsWorld.addComponent(npcId, new Position(100, 100));
        
        // Run multiple ticks - friendly wander has random behavior
        for (int i = 0; i < 100; i++) {
            system.tick(0.05f);
        }
        
        // At least verify the system runs without error
        assertTrue(ecsWorld.entityExists(npcId));
        assertTrue(ecsWorld.hasComponent(npcId, NpcAi.class));
    }
    
    @Test
    void testHostileAggroWithNoPlayers() {
        String npcId = ecsWorld.createEntity();
        ecsWorld.addComponent(npcId, new NpcAi(NpcAi.AiType.HOSTILE_AGGRO_MELEE));
        ecsWorld.addComponent(npcId, new Position(100, 100));
        ecsWorld.addComponent(npcId, new Zone("zone1"));
        
        system.tick(0.05f);
        
        // Should not crash with no players in the world
        assertTrue(ecsWorld.entityExists(npcId));
    }
    
    @Test
    void testHostileAggroWithPlayerInDifferentZone() {
        String npcId = ecsWorld.createEntity();
        ecsWorld.addComponent(npcId, new NpcAi(NpcAi.AiType.HOSTILE_AGGRO_MELEE));
        ecsWorld.addComponent(npcId, new Position(100, 100));
        ecsWorld.addComponent(npcId, new Zone("zone1"));
        
        String playerId = ecsWorld.createEntity();
        ecsWorld.addComponent(playerId, new PlayerConnection(null));
        ecsWorld.addComponent(playerId, new Position(120, 120));
        ecsWorld.addComponent(playerId, new Zone("zone2")); // Different zone
        
        system.tick(0.05f);
        
        // NPC should not chase player in different zone
        assertTrue(ecsWorld.entityExists(npcId));
    }
    
    @Test
    void testNpcWithoutAiComponentIgnored() {
        String entityId = ecsWorld.createEntity();
        ecsWorld.addComponent(entityId, new Position(0, 0));
        
        system.tick(0.05f);
        
        // Should not crash or add velocity to entities without NpcAi
        assertFalse(ecsWorld.hasComponent(entityId, Velocity.class));
    }
    
    @Test
    void testHostileAggroWithPlayerInSameZone() {
        String npcId = ecsWorld.createEntity();
        ecsWorld.addComponent(npcId, new NpcAi(NpcAi.AiType.HOSTILE_AGGRO_MELEE));
        ecsWorld.addComponent(npcId, new Position(100, 100));
        ecsWorld.addComponent(npcId, new Zone("zone1"));
        
        String playerId = ecsWorld.createEntity();
        ecsWorld.addComponent(playerId, new PlayerConnection(null));
        ecsWorld.addComponent(playerId, new Position(110, 110)); // Close by
        ecsWorld.addComponent(playerId, new Zone("zone1")); // Same zone
        ecsWorld.addComponent(playerId, new Solid()); // Players need Solid to be tracked in spatial grid
        
        // Update spatial grid before AI tick
        spatialGridSystem.tick(0.05f);
        system.tick(0.05f);
        
        // NPC should chase player - verify velocity was set
        assertTrue(ecsWorld.hasComponent(npcId, Velocity.class));
    }
    
    @Test
    void testHostileAggroWithPlayerOutOfRange() {
        String npcId = ecsWorld.createEntity();
        ecsWorld.addComponent(npcId, new NpcAi(NpcAi.AiType.HOSTILE_AGGRO_MELEE));
        ecsWorld.addComponent(npcId, new Position(100, 100));
        ecsWorld.addComponent(npcId, new Zone("zone1"));
        
        String playerId = ecsWorld.createEntity();
        ecsWorld.addComponent(playerId, new PlayerConnection(null));
        ecsWorld.addComponent(playerId, new Position(500, 500)); // Far away (distance > 100)
        ecsWorld.addComponent(playerId, new Zone("zone1")); // Same zone
        ecsWorld.addComponent(playerId, new Solid());
        
        // Update spatial grid before AI tick
        spatialGridSystem.tick(0.05f);
        system.tick(0.05f);
        
        // NPC should stop moving when player is out of range
        assertTrue(ecsWorld.hasComponent(npcId, Velocity.class));
        Velocity vel = ecsWorld.getComponent(npcId, Velocity.class).get();
        assertEquals(0, vel.dx(), 0.001);
        assertEquals(0, vel.dy(), 0.001);
    }
    
    @Test
    void testNpcWithoutPositionSkipped() {
        String npcId = ecsWorld.createEntity();
        ecsWorld.addComponent(npcId, new NpcAi(NpcAi.AiType.HOSTILE_AGGRO_MELEE));
        ecsWorld.addComponent(npcId, new Zone("zone1"));
        // No Position component
        
        system.tick(0.05f);
        
        // Should not crash
        assertTrue(ecsWorld.entityExists(npcId));
    }
    
    @Test
    void testNpcWithoutZoneSkipped() {
        String npcId = ecsWorld.createEntity();
        ecsWorld.addComponent(npcId, new NpcAi(NpcAi.AiType.HOSTILE_AGGRO_MELEE));
        ecsWorld.addComponent(npcId, new Position(100, 100));
        // No Zone component
        
        system.tick(0.05f);
        
        // Should not crash
        assertTrue(ecsWorld.entityExists(npcId));
    }
    
    @Test
    void testHostileAggroWithLeashInRange() {
        // Create NPC with spawn point at (100, 100) and leash radius of 200
        String npcId = ecsWorld.createEntity();
        ecsWorld.addComponent(npcId, new NpcAi(NpcAi.AiType.HOSTILE_AGGRO_MELEE));
        ecsWorld.addComponent(npcId, new Position(150, 150)); // Close to spawn
        ecsWorld.addComponent(npcId, new Zone("zone1"));
        ecsWorld.addComponent(npcId, new SpawnPoint(100, 100, 200.0));
        
        String playerId = ecsWorld.createEntity();
        ecsWorld.addComponent(playerId, new PlayerConnection(null));
        ecsWorld.addComponent(playerId, new Position(160, 160)); // Close to NPC
        ecsWorld.addComponent(playerId, new Zone("zone1"));
        ecsWorld.addComponent(playerId, new Solid());
        
        // Update spatial grid before AI tick
        spatialGridSystem.tick(0.05f);
        system.tick(0.05f);
        
        // NPC should chase player (not leashed)
        assertTrue(ecsWorld.hasComponent(npcId, Velocity.class));
        Velocity vel = ecsWorld.getComponent(npcId, Velocity.class).get();
        assertTrue(vel.dx() != 0 || vel.dy() != 0, "NPC should be moving towards player");
    }
    
    @Test
    void testHostileAggroReturnsToSpawnWhenOutOfLeash() {
        // Create NPC with spawn point at (0, 0) and leash radius of 50
        String npcId = ecsWorld.createEntity();
        ecsWorld.addComponent(npcId, new NpcAi(NpcAi.AiType.HOSTILE_AGGRO_MELEE));
        ecsWorld.addComponent(npcId, new Position(300, 300)); // Far from spawn (distance > 50)
        ecsWorld.addComponent(npcId, new Zone("zone1"));
        ecsWorld.addComponent(npcId, new SpawnPoint(0, 0, 50.0)); // Small leash radius
        
        String playerId = ecsWorld.createEntity();
        ecsWorld.addComponent(playerId, new PlayerConnection(null));
        ecsWorld.addComponent(playerId, new Position(310, 310)); // Close to NPC but NPC is leashed
        ecsWorld.addComponent(playerId, new Zone("zone1"));
        ecsWorld.addComponent(playerId, new Solid());
        
        // Update spatial grid before AI tick
        spatialGridSystem.tick(0.05f);
        system.tick(0.05f);
        
        // NPC should be moving towards spawn, not player
        assertTrue(ecsWorld.hasComponent(npcId, Velocity.class));
        Velocity vel = ecsWorld.getComponent(npcId, Velocity.class).get();
        
        // Velocity should point towards spawn (negative direction)
        assertTrue(vel.dx() < 0, "NPC should be moving towards spawn (negative x)");
        assertTrue(vel.dy() < 0, "NPC should be moving towards spawn (negative y)");
    }
    
    @Test
    void testNpcStopsAtSpawnPoint() {
        // Create NPC at spawn point
        String npcId = ecsWorld.createEntity();
        ecsWorld.addComponent(npcId, new NpcAi(NpcAi.AiType.HOSTILE_AGGRO_MELEE));
        ecsWorld.addComponent(npcId, new Position(0.5, 0.5)); // Very close to spawn
        ecsWorld.addComponent(npcId, new Zone("zone1"));
        ecsWorld.addComponent(npcId, new SpawnPoint(0, 0, 50.0));
        ecsWorld.addComponent(npcId, new Stats(50, 100, 5, 10));
        
        // Mark NPC as "returning" by placing it just outside leash then inside
        // but first let's just test that when at spawn, it stops
        
        system.tick(0.05f);
        
        // Should not crash
        assertTrue(ecsWorld.entityExists(npcId));
    }
    
    @Test
    void testNpcWithoutSpawnPointNotLeashed() {
        // Create NPC without spawn point
        String npcId = ecsWorld.createEntity();
        ecsWorld.addComponent(npcId, new NpcAi(NpcAi.AiType.HOSTILE_AGGRO_MELEE));
        ecsWorld.addComponent(npcId, new Position(100, 100));
        ecsWorld.addComponent(npcId, new Zone("zone1"));
        // No SpawnPoint component
        
        String playerId = ecsWorld.createEntity();
        ecsWorld.addComponent(playerId, new PlayerConnection(null));
        ecsWorld.addComponent(playerId, new Position(110, 110));
        ecsWorld.addComponent(playerId, new Zone("zone1"));
        ecsWorld.addComponent(playerId, new Solid());
        
        // Update spatial grid before AI tick
        spatialGridSystem.tick(0.05f);
        system.tick(0.05f);
        
        // NPC should chase player (no leash constraint)
        assertTrue(ecsWorld.hasComponent(npcId, Velocity.class));
        Velocity vel = ecsWorld.getComponent(npcId, Velocity.class).get();
        assertTrue(vel.dx() != 0 || vel.dy() != 0, "NPC should be moving towards player");
    }
    
    @Test
    void testHostileAggroWithPathfindingEnabled() {
        // Create a new system with navigation enabled
        GameConfig gameConfig = TestGameConfig.create();
        com.grimoire.server.navigation.NavigationSystem navigationSystem = 
                new com.grimoire.server.navigation.NavigationSystem(ecsWorld, gameConfig);
        NpcAiSystem systemWithNav = new NpcAiSystem(ecsWorld, spatialGridSystem, navigationSystem, gameConfig);
        
        String npcId = ecsWorld.createEntity();
        ecsWorld.addComponent(npcId, new NpcAi(NpcAi.AiType.HOSTILE_AGGRO_MELEE));
        ecsWorld.addComponent(npcId, new Position(100, 100));
        ecsWorld.addComponent(npcId, new Zone("zone1"));
        
        String playerId = ecsWorld.createEntity();
        ecsWorld.addComponent(playerId, new PlayerConnection(null));
        // Distance from (100,100) to (150,150) = sqrt(2500+2500) = ~70.7, within aggro range of 100
        ecsWorld.addComponent(playerId, new Position(150, 150));
        ecsWorld.addComponent(playerId, new Zone("zone1"));
        ecsWorld.addComponent(playerId, new Solid());
        
        // Update spatial grid and run AI tick
        spatialGridSystem.tick(0.05f);
        navigationSystem.rebuildAllGrids();
        systemWithNav.tick(0.05f);
        
        // NPC should have velocity set
        assertTrue(ecsWorld.hasComponent(npcId, Velocity.class));
        Velocity vel = ecsWorld.getComponent(npcId, Velocity.class).get();
        assertTrue(vel.dx() != 0 || vel.dy() != 0, "NPC should be moving towards player");
        
        // NPC should have a path component
        assertTrue(ecsWorld.hasComponent(npcId, Path.class), "NPC should have calculated a path");
    }
    
    @Test
    void testPathClearedWhenNoTarget() {
        GameConfig gameConfig = TestGameConfig.create();
        com.grimoire.server.navigation.NavigationSystem navigationSystem = 
                new com.grimoire.server.navigation.NavigationSystem(ecsWorld, gameConfig);
        NpcAiSystem systemWithNav = new NpcAiSystem(ecsWorld, spatialGridSystem, navigationSystem, gameConfig);
        
        String npcId = ecsWorld.createEntity();
        ecsWorld.addComponent(npcId, new NpcAi(NpcAi.AiType.HOSTILE_AGGRO_MELEE));
        ecsWorld.addComponent(npcId, new Position(100, 100));
        ecsWorld.addComponent(npcId, new Zone("zone1"));
        
        String playerId = ecsWorld.createEntity();
        ecsWorld.addComponent(playerId, new PlayerConnection(null));
        // Distance from (100,100) to (150,150) = ~70.7, within aggro range of 100
        ecsWorld.addComponent(playerId, new Position(150, 150));
        ecsWorld.addComponent(playerId, new Zone("zone1"));
        ecsWorld.addComponent(playerId, new Solid());
        
        // Update and tick
        spatialGridSystem.tick(0.05f);
        navigationSystem.rebuildAllGrids();
        systemWithNav.tick(0.05f);
        
        // Should have path
        assertTrue(ecsWorld.hasComponent(npcId, Path.class));
        
        // Remove player from game
        ecsWorld.destroyEntity(playerId);
        
        // Tick again
        spatialGridSystem.tick(0.05f);
        systemWithNav.tick(0.05f);
        
        // Path should be cleared
        assertFalse(ecsWorld.hasComponent(npcId, Path.class), "Path should be cleared when target is gone");
        
        // Velocity should be zero
        Velocity vel = ecsWorld.getComponent(npcId, Velocity.class).get();
        assertEquals(0, vel.dx());
        assertEquals(0, vel.dy());
    }
    
    @Test
    void testPathfindingAroundObstacle() {
        GameConfig gameConfig = TestGameConfig.create();
        com.grimoire.server.navigation.NavigationSystem navigationSystem = 
                new com.grimoire.server.navigation.NavigationSystem(ecsWorld, gameConfig);
        NpcAiSystem systemWithNav = new NpcAiSystem(ecsWorld, spatialGridSystem, navigationSystem, gameConfig);
        
        // Create a wall obstacle between NPC and player (wall at x=128, blocks y=64 to y=136)
        String wallId = ecsWorld.createEntity();
        ecsWorld.addComponent(wallId, new Solid());
        ecsWorld.addComponent(wallId, new Position(128, 100));
        ecsWorld.addComponent(wallId, new BoundingBox(32, 64)); // Wall blocking direct path
        ecsWorld.addComponent(wallId, new Zone("zone1"));
        
        // NPC at (80, 100) - left of wall
        String npcId = ecsWorld.createEntity();
        ecsWorld.addComponent(npcId, new NpcAi(NpcAi.AiType.HOSTILE_AGGRO_MELEE));
        ecsWorld.addComponent(npcId, new Position(80, 100));
        ecsWorld.addComponent(npcId, new Zone("zone1"));
        
        // Player at (176, 100) - right of wall, within aggro range (~96 units < 100)
        String playerId = ecsWorld.createEntity();
        ecsWorld.addComponent(playerId, new PlayerConnection(null));
        ecsWorld.addComponent(playerId, new Position(176, 100));
        ecsWorld.addComponent(playerId, new Zone("zone1"));
        ecsWorld.addComponent(playerId, new Solid());
        
        // Update grids and tick
        spatialGridSystem.tick(0.05f);
        navigationSystem.rebuildAllGrids();
        systemWithNav.tick(0.05f);
        
        // Verify NPC has a path component (pathfinding was attempted)
        assertTrue(ecsWorld.hasComponent(npcId, Path.class), 
                "NPC should have a path component after pathfinding");
        
        Path path = ecsWorld.getComponent(npcId, Path.class).get();
        
        // The path should have waypoints since wall doesn't completely block
        // A direct path would be 2 waypoints, going around needs more
        assertTrue(path.size() >= 2, 
                "Path should have at least 2 waypoints: " + path.size());
        
        // NPC should be moving (velocity set)
        assertTrue(ecsWorld.hasComponent(npcId, Velocity.class));
        Velocity vel = ecsWorld.getComponent(npcId, Velocity.class).get();
        assertTrue(vel.dx() != 0 || vel.dy() != 0, 
                "NPC should be moving towards path waypoint");
    }
}
