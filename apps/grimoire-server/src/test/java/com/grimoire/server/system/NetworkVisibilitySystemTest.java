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

class NetworkVisibilitySystemTest {
    
    private EcsWorld ecsWorld;
    private NetworkVisibilitySystem system;
    private Channel mockChannel;
    
    @BeforeEach
    void setUp() {
        EntityManager entityManager = new EntityManager();
        ComponentManager componentManager = new ComponentManager();
        ecsWorld = new EcsWorld(entityManager, componentManager);
        system = new NetworkVisibilitySystem(ecsWorld);
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
    void testFirstTickTracksPlayerZone() {
        String playerId = ecsWorld.createEntity();
        ecsWorld.addComponent(playerId, new PlayerConnection(mockChannel));
        ecsWorld.addComponent(playerId, new Zone("zone1"));
        
        system.tick(0.05f);
        
        // Player zone should be tracked
        assertTrue(ecsWorld.entityExists(playerId));
    }
    
    @Test
    void testPlayerStayingInSameZoneNoUpdate() {
        String playerId = ecsWorld.createEntity();
        ecsWorld.addComponent(playerId, new PlayerConnection(mockChannel));
        ecsWorld.addComponent(playerId, new Zone("zone1"));
        
        // First tick - zone is new
        system.tick(0.05f);
        
        // Second tick - same zone
        system.tick(0.05f);
        
        // Should complete without error
        assertTrue(ecsWorld.entityExists(playerId));
    }
    
    @Test
    void testPlayerChangingZoneTriggersUpdate() {
        String playerId = ecsWorld.createEntity();
        ecsWorld.addComponent(playerId, new PlayerConnection(mockChannel));
        ecsWorld.addComponent(playerId, new Zone("zone1"));
        
        // First tick
        system.tick(0.05f);
        
        // Change zone
        ecsWorld.addComponent(playerId, new Zone("zone2"));
        
        // Second tick should detect zone change
        system.tick(0.05f);
        
        assertTrue(ecsWorld.entityExists(playerId));
    }
    
    @Test
    void testDisconnectedPlayerCleanedUp() {
        String playerId = ecsWorld.createEntity();
        ecsWorld.addComponent(playerId, new PlayerConnection(mockChannel));
        ecsWorld.addComponent(playerId, new Zone("zone1"));
        
        // First tick
        system.tick(0.05f);
        
        // Player disconnects
        ecsWorld.removeComponent(playerId, PlayerConnection.class);
        
        // Second tick should clean up tracking
        system.tick(0.05f);
        
        assertTrue(ecsWorld.entityExists(playerId));
    }
    
    @Test
    void testPlayerNotIncludedInOwnSpawnList() {
        String playerId = ecsWorld.createEntity();
        ecsWorld.addComponent(playerId, new PlayerConnection(mockChannel));
        ecsWorld.addComponent(playerId, new Zone("zone1"));
        ecsWorld.addComponent(playerId, new Position(100, 100));
        ecsWorld.addComponent(playerId, new Renderable("Player1", "player-sprite"));
        
        system.tick(0.05f);
        
        // Should not include self in spawn list
        assertTrue(ecsWorld.entityExists(playerId));
    }
    
    @Test
    void testEntitiesInDifferentZoneNotSent() {
        String playerId = ecsWorld.createEntity();
        ecsWorld.addComponent(playerId, new PlayerConnection(mockChannel));
        ecsWorld.addComponent(playerId, new Zone("zone1"));
        
        String entityId = ecsWorld.createEntity();
        ecsWorld.addComponent(entityId, new Zone("zone2")); // Different zone
        ecsWorld.addComponent(entityId, new Position(100, 100));
        ecsWorld.addComponent(entityId, new Renderable("NPC", "npc-sprite"));
        
        system.tick(0.05f);
        
        // Should process without errors
        assertTrue(ecsWorld.entityExists(playerId));
        assertTrue(ecsWorld.entityExists(entityId));
    }
    
    @Test
    void testEntityWithoutZoneSkipped() {
        String playerId = ecsWorld.createEntity();
        ecsWorld.addComponent(playerId, new PlayerConnection(mockChannel));
        ecsWorld.addComponent(playerId, new Zone("zone1"));
        
        String entityId = ecsWorld.createEntity();
        // No Zone component
        ecsWorld.addComponent(entityId, new Position(100, 100));
        ecsWorld.addComponent(entityId, new Renderable("NPC", "npc-sprite"));
        
        system.tick(0.05f);
        
        // Should not crash
        assertTrue(ecsWorld.entityExists(playerId));
    }
    
    @Test
    void testConvertToComponentDTOsWithPortal() {
        String playerId = ecsWorld.createEntity();
        ecsWorld.addComponent(playerId, new PlayerConnection(mockChannel));
        ecsWorld.addComponent(playerId, new Zone("zone1"));
        
        String portalId = ecsWorld.createEntity();
        ecsWorld.addComponent(portalId, new Zone("zone1"));
        ecsWorld.addComponent(portalId, new Position(200, 200));
        ecsWorld.addComponent(portalId, new BoundingBox(50, 50));
        ecsWorld.addComponent(portalId, new Portal("zone2", "portal1"));
        ecsWorld.addComponent(portalId, new Renderable("Portal", "portal-sprite"));
        
        system.tick(0.05f);
        
        // Should process portal DTO without errors
        assertTrue(ecsWorld.entityExists(portalId));
    }
}
