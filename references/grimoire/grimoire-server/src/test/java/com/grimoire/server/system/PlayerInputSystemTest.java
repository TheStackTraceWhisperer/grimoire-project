package com.grimoire.server.system;

import com.grimoire.server.component.*;
import com.grimoire.server.config.TestGameConfig;
import com.grimoire.server.ecs.ComponentManager;
import com.grimoire.server.ecs.EcsWorld;
import com.grimoire.server.ecs.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PlayerInputSystemTest {
    
    private EcsWorld ecsWorld;
    private PlayerInputSystem system;
    
    @BeforeEach
    void setUp() {
        EntityManager entityManager = new EntityManager();
        ComponentManager componentManager = new ComponentManager();
        ecsWorld = new EcsWorld(entityManager, componentManager);
        system = new PlayerInputSystem(ecsWorld, TestGameConfig.create());
    }
    
    @Test
    void testPlayerMovementToTarget() {
        String playerId = ecsWorld.createEntity();
        ecsWorld.addComponent(playerId, new PlayerConnection(null));
        ecsWorld.addComponent(playerId, new Position(0, 0));
        ecsWorld.addComponent(playerId, new MovementIntent(100, 0));
        
        system.tick(0.05f);
        
        // Should add velocity component
        assertTrue(ecsWorld.hasComponent(playerId, Velocity.class));
        Velocity velocity = ecsWorld.getComponent(playerId, Velocity.class).get();
        
        // Velocity should be normalized to speed (5.0)
        assertEquals(5.0, velocity.dx(), 0.001);
        assertEquals(0.0, velocity.dy(), 0.001);
        
        // MovementIntent should be removed
        assertFalse(ecsWorld.hasComponent(playerId, MovementIntent.class));
    }
    
    @Test
    void testPlayerStopsAtDestination() {
        String playerId = ecsWorld.createEntity();
        ecsWorld.addComponent(playerId, new PlayerConnection(null));
        ecsWorld.addComponent(playerId, new Position(0, 0));
        ecsWorld.addComponent(playerId, new MovementIntent(0, 0)); // Already at target
        
        system.tick(0.05f);
        
        // Should set velocity to zero
        assertTrue(ecsWorld.hasComponent(playerId, Velocity.class));
        Velocity velocity = ecsWorld.getComponent(playerId, Velocity.class).get();
        assertEquals(0.0, velocity.dx());
        assertEquals(0.0, velocity.dy());
        
        // MovementIntent should be removed
        assertFalse(ecsWorld.hasComponent(playerId, MovementIntent.class));
    }
    
    @Test
    void testNoPlayerConnectionIgnored() {
        String entityId = ecsWorld.createEntity();
        ecsWorld.addComponent(entityId, new Position(0, 0));
        ecsWorld.addComponent(entityId, new MovementIntent(100, 100));
        
        system.tick(0.05f);
        
        // Should not process entities without PlayerConnection
        assertFalse(ecsWorld.hasComponent(entityId, Velocity.class));
        // MovementIntent should remain
        assertTrue(ecsWorld.hasComponent(entityId, MovementIntent.class));
    }
}
