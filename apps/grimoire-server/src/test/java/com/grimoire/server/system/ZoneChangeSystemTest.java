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

class ZoneChangeSystemTest {
    
    private EcsWorld ecsWorld;
    private ZoneChangeSystem system;
    private Channel mockChannel;
    
    @BeforeEach
    void setUp() {
        EntityManager entityManager = new EntityManager();
        ComponentManager componentManager = new ComponentManager();
        ecsWorld = new EcsWorld(entityManager, componentManager);
        system = new ZoneChangeSystem(ecsWorld);
        mockChannel = Mockito.mock(Channel.class);
    }
    
    @Test
    void testPlayerWithoutRequiredComponentsSkipped() {
        String playerId = ecsWorld.createEntity();
        ecsWorld.addComponent(playerId, new PlayerConnection(mockChannel));
        
        system.tick(0.05f);
        
        // Should not crash when player lacks required components
        assertTrue(ecsWorld.entityExists(playerId));
    }
    
    @Test
    void testPlayerWithPortalCooldownSkipped() {
        String playerId = ecsWorld.createEntity();
        ecsWorld.addComponent(playerId, new PlayerConnection(mockChannel));
        ecsWorld.addComponent(playerId, new Zone("zone1"));
        ecsWorld.addComponent(playerId, new Position(100, 100));
        ecsWorld.addComponent(playerId, new BoundingBox(10, 10));
        ecsWorld.addComponent(playerId, new PortalCooldown(30));
        
        String portalId = ecsWorld.createEntity();
        ecsWorld.addComponent(portalId, new Zone("zone1"));
        ecsWorld.addComponent(portalId, new Position(100, 100));
        ecsWorld.addComponent(portalId, new BoundingBox(20, 20));
        ecsWorld.addComponent(portalId, new Portal("zone2", "target-portal"));
        
        Zone playerZoneBefore = ecsWorld.getComponent(playerId, Zone.class).get();
        
        system.tick(0.05f);
        
        // Player zone should not change when cooldown active
        Zone playerZoneAfter = ecsWorld.getComponent(playerId, Zone.class).get();
        assertEquals(playerZoneBefore.zoneId(), playerZoneAfter.zoneId());
    }
    
    @Test
    void testPortalInDifferentZoneIgnored() {
        String playerId = ecsWorld.createEntity();
        ecsWorld.addComponent(playerId, new PlayerConnection(mockChannel));
        ecsWorld.addComponent(playerId, new Zone("zone1"));
        ecsWorld.addComponent(playerId, new Position(100, 100));
        ecsWorld.addComponent(playerId, new BoundingBox(10, 10));
        
        String portalId = ecsWorld.createEntity();
        ecsWorld.addComponent(portalId, new Zone("zone2")); // Different zone
        ecsWorld.addComponent(portalId, new Position(100, 100));
        ecsWorld.addComponent(portalId, new BoundingBox(20, 20));
        ecsWorld.addComponent(portalId, new Portal("zone3", "target-portal"));
        
        system.tick(0.05f);
        
        // Player should remain in zone1
        assertEquals("zone1", ecsWorld.getComponent(playerId, Zone.class).get().zoneId());
    }
    
    @Test
    void testPortalWithoutRequiredComponentsSkipped() {
        String playerId = ecsWorld.createEntity();
        ecsWorld.addComponent(playerId, new PlayerConnection(mockChannel));
        ecsWorld.addComponent(playerId, new Zone("zone1"));
        ecsWorld.addComponent(playerId, new Position(100, 100));
        ecsWorld.addComponent(playerId, new BoundingBox(10, 10));
        
        String portalId = ecsWorld.createEntity();
        ecsWorld.addComponent(portalId, new Zone("zone1"));
        ecsWorld.addComponent(portalId, new Portal("zone2", "target-portal"));
        // Missing Position and BoundingBox
        
        system.tick(0.05f);
        
        // Should not crash
        assertTrue(ecsWorld.entityExists(playerId));
    }
    
    @Test
    void testNoCollisionWithPortal() {
        String playerId = ecsWorld.createEntity();
        ecsWorld.addComponent(playerId, new PlayerConnection(mockChannel));
        ecsWorld.addComponent(playerId, new Zone("zone1"));
        ecsWorld.addComponent(playerId, new Position(100, 100));
        ecsWorld.addComponent(playerId, new BoundingBox(10, 10));
        
        String portalId = ecsWorld.createEntity();
        ecsWorld.addComponent(portalId, new Zone("zone1"));
        ecsWorld.addComponent(portalId, new Position(500, 500)); // Far away
        ecsWorld.addComponent(portalId, new BoundingBox(20, 20));
        ecsWorld.addComponent(portalId, new Portal("zone2", "target-portal"));
        
        system.tick(0.05f);
        
        // Player should remain in zone1
        assertEquals("zone1", ecsWorld.getComponent(playerId, Zone.class).get().zoneId());
        assertFalse(ecsWorld.hasComponent(playerId, PortalCooldown.class));
    }
    
    @Test
    void testTargetPortalNotFoundSkipped() {
        String playerId = ecsWorld.createEntity();
        ecsWorld.addComponent(playerId, new PlayerConnection(mockChannel));
        ecsWorld.addComponent(playerId, new Zone("zone1"));
        ecsWorld.addComponent(playerId, new Position(100, 100));
        ecsWorld.addComponent(playerId, new BoundingBox(10, 10));
        
        String portalId = ecsWorld.createEntity();
        ecsWorld.addComponent(portalId, new Zone("zone1"));
        ecsWorld.addComponent(portalId, new Position(100, 100));
        ecsWorld.addComponent(portalId, new BoundingBox(20, 20));
        ecsWorld.addComponent(portalId, new Portal("zone2", "nonexistent-portal"));
        // No target portal exists
        
        system.tick(0.05f);
        
        // Player should remain in zone1 when target portal not found
        assertEquals("zone1", ecsWorld.getComponent(playerId, Zone.class).get().zoneId());
    }
}
