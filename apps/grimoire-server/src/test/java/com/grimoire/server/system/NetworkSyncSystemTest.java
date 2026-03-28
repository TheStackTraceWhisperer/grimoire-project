package com.grimoire.server.system;

import com.grimoire.server.component.*;
import com.grimoire.ecs.ComponentManager;
import com.grimoire.ecs.EcsWorld;
import com.grimoire.ecs.EntityManager;
import io.netty.channel.Channel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

class NetworkSyncSystemTest {
    
    private EcsWorld ecsWorld;
    private NetworkSyncSystem system;
    private Channel mockChannel;
    
    @BeforeEach
    void setUp() {
        EntityManager entityManager = new EntityManager();
        ComponentManager componentManager = new ComponentManager();
        ecsWorld = new EcsWorld(entityManager, componentManager);
        system = new NetworkSyncSystem(ecsWorld);
        mockChannel = Mockito.mock(Channel.class);
    }
    
    @Test
    void testPlayerWithoutZoneSkipped() {
        String playerId = ecsWorld.createEntity();
        ecsWorld.addComponent(playerId, new PlayerConnection(mockChannel));
        // No Zone component
        
        system.tick(0.05f);
        
        // Should not crash
        assertTrue(ecsWorld.entityExists(playerId));
    }
    
    @Test
    void testPlayerWithoutConnectionSkipped() {
        String playerId = ecsWorld.createEntity();
        ecsWorld.addComponent(playerId, new Zone("zone1"));
        // No PlayerConnection component
        
        system.tick(0.05f);
        
        // Should not crash
        assertTrue(ecsWorld.entityExists(playerId));
    }
    
    @Test
    void testNoDirtyEntitiesNoUpdate() {
        String playerId = ecsWorld.createEntity();
        ecsWorld.addComponent(playerId, new PlayerConnection(mockChannel));
        ecsWorld.addComponent(playerId, new Zone("zone1"));
        
        // No dirty entities
        system.tick(0.05f);
        
        // Should complete without error
        assertTrue(ecsWorld.entityExists(playerId));
    }
    
    @Test
    void testDirtyComponentsRemoved() {
        String entityId = ecsWorld.createEntity();
        ecsWorld.addComponent(entityId, new Zone("zone1"));
        ecsWorld.addComponent(entityId, new Position(100, 100));
        ecsWorld.addComponent(entityId, new Dirty(1L));
        
        assertTrue(ecsWorld.hasComponent(entityId, Dirty.class));
        
        system.tick(0.05f);
        
        // Dirty component should be removed after processing
        assertFalse(ecsWorld.hasComponent(entityId, Dirty.class));
    }
    
    @Test
    void testEntityInDifferentZoneNotSynced() {
        String playerId = ecsWorld.createEntity();
        ecsWorld.addComponent(playerId, new PlayerConnection(mockChannel));
        ecsWorld.addComponent(playerId, new Zone("zone1"));
        
        String entityId = ecsWorld.createEntity();
        ecsWorld.addComponent(entityId, new Zone("zone2")); // Different zone
        ecsWorld.addComponent(entityId, new Position(100, 100));
        ecsWorld.addComponent(entityId, new Dirty(1L));
        
        system.tick(0.05f);
        
        // Dirty component should still be removed
        assertFalse(ecsWorld.hasComponent(entityId, Dirty.class));
    }
    
    @Test
    void testEntityWithoutZoneSkipped() {
        String playerId = ecsWorld.createEntity();
        ecsWorld.addComponent(playerId, new PlayerConnection(mockChannel));
        ecsWorld.addComponent(playerId, new Zone("zone1"));
        
        String entityId = ecsWorld.createEntity();
        // No Zone component
        ecsWorld.addComponent(entityId, new Position(100, 100));
        ecsWorld.addComponent(entityId, new Dirty(1L));
        
        system.tick(0.05f);
        
        // Should not crash
        assertFalse(ecsWorld.hasComponent(entityId, Dirty.class));
    }
    
    @Test
    void testConvertToComponentDTOs() {
        String entityId = ecsWorld.createEntity();
        ecsWorld.addComponent(entityId, new Zone("zone1"));
        ecsWorld.addComponent(entityId, new Position(100, 200));
        ecsWorld.addComponent(entityId, new Renderable("Player", "player-sprite"));
        ecsWorld.addComponent(entityId, new Stats(80, 100, 10, 10));
        ecsWorld.addComponent(entityId, new Dirty(1L));
        
        String playerId = ecsWorld.createEntity();
        ecsWorld.addComponent(playerId, new PlayerConnection(mockChannel));
        ecsWorld.addComponent(playerId, new Zone("zone1"));
        
        system.tick(0.05f);
        
        // Should process without errors
        assertFalse(ecsWorld.hasComponent(entityId, Dirty.class));
    }
    
    @Test
    void testPortalWithBoundingBoxConverted() {
        String entityId = ecsWorld.createEntity();
        ecsWorld.addComponent(entityId, new Zone("zone1"));
        ecsWorld.addComponent(entityId, new Position(100, 200));
        ecsWorld.addComponent(entityId, new BoundingBox(50, 50));
        ecsWorld.addComponent(entityId, new Portal("zone2", "portal1"));
        ecsWorld.addComponent(entityId, new Dirty(1L));
        
        String playerId = ecsWorld.createEntity();
        ecsWorld.addComponent(playerId, new PlayerConnection(mockChannel));
        ecsWorld.addComponent(playerId, new Zone("zone1"));
        
        system.tick(0.05f);
        
        // Should process portal DTO without errors
        assertFalse(ecsWorld.hasComponent(entityId, Dirty.class));
    }
}
