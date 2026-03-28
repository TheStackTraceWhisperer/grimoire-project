package com.grimoire.server.content;

import com.grimoire.server.component.*;
import com.grimoire.ecs.ComponentManager;
import com.grimoire.ecs.EcsWorld;
import com.grimoire.ecs.EntityManager;
import io.micronaut.runtime.event.ApplicationStartupEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

class ZoneInitializerTest {
    
    private EcsWorld ecsWorld;
    private NpcFactory npcFactory;
    private ZoneInitializer zoneInitializer;
    private PrefabRegistry prefabRegistry;
    
    @BeforeEach
    void setUp() {
        EntityManager entityManager = new EntityManager();
        ComponentManager componentManager = new ComponentManager();
        ecsWorld = new EcsWorld(entityManager, componentManager);
        prefabRegistry = new PrefabRegistry(ecsWorld);
        prefabRegistry.onApplicationEvent(null); // Register prefabs
        npcFactory = new NpcFactory(ecsWorld);
        zoneInitializer = new ZoneInitializer(npcFactory);
    }
    
    @Test
    void testOnApplicationEventInitializesZones() {
        ApplicationStartupEvent mockEvent = Mockito.mock(ApplicationStartupEvent.class);
        
        int entitiesBeforeInit = 0;
        for (String entityId : ecsWorld.getAllEntities()) {
            entitiesBeforeInit++;
        }
        
        zoneInitializer.onApplicationEvent(mockEvent);
        
        int entitiesAfterInit = 0;
        for (String entityId : ecsWorld.getAllEntities()) {
            entitiesAfterInit++;
        }
        
        // Should have created many entities (NPCs, monsters, portals)
        assertTrue(entitiesAfterInit > entitiesBeforeInit);
        assertTrue(entitiesAfterInit > 20); // At least 20+ entities across all zones
    }
    
    @Test
    void testZone1Entities() {
        ApplicationStartupEvent mockEvent = Mockito.mock(ApplicationStartupEvent.class);
        zoneInitializer.onApplicationEvent(mockEvent);
        
        // Count entities in zone1
        int zone1Count = 0;
        for (String entityId : ecsWorld.getAllEntities()) {
            if (ecsWorld.getComponent(entityId, Zone.class)
                    .map(z -> z.zoneId().equals("zone1"))
                    .orElse(false)) {
                zone1Count++;
            }
        }
        
        // Zone1 should have NPCs, monsters, and at least one portal
        assertTrue(zone1Count > 0);
    }
    
    @Test
    void testZone2Entities() {
        ApplicationStartupEvent mockEvent = Mockito.mock(ApplicationStartupEvent.class);
        zoneInitializer.onApplicationEvent(mockEvent);
        
        // Count entities in zone2
        int zone2Count = 0;
        for (String entityId : ecsWorld.getAllEntities()) {
            if (ecsWorld.getComponent(entityId, Zone.class)
                    .map(z -> z.zoneId().equals("zone2"))
                    .orElse(false)) {
                zone2Count++;
            }
        }
        
        // Zone2 should have monsters and portals
        assertTrue(zone2Count > 0);
    }
    
    @Test
    void testZone3Entities() {
        ApplicationStartupEvent mockEvent = Mockito.mock(ApplicationStartupEvent.class);
        zoneInitializer.onApplicationEvent(mockEvent);
        
        // Count entities in zone3
        int zone3Count = 0;
        for (String entityId : ecsWorld.getAllEntities()) {
            if (ecsWorld.getComponent(entityId, Zone.class)
                    .map(z -> z.zoneId().equals("zone3"))
                    .orElse(false)) {
                zone3Count++;
            }
        }
        
        // Zone3 should have monsters and portals
        assertTrue(zone3Count > 0);
    }
    
    @Test
    void testZone4Entities() {
        ApplicationStartupEvent mockEvent = Mockito.mock(ApplicationStartupEvent.class);
        zoneInitializer.onApplicationEvent(mockEvent);
        
        // Count entities in zone4
        int zone4Count = 0;
        for (String entityId : ecsWorld.getAllEntities()) {
            if (ecsWorld.getComponent(entityId, Zone.class)
                    .map(z -> z.zoneId().equals("zone4"))
                    .orElse(false)) {
                zone4Count++;
            }
        }
        
        // Zone4 should have monsters and portals
        assertTrue(zone4Count > 0);
    }
    
    @Test
    void testZone5Entities() {
        ApplicationStartupEvent mockEvent = Mockito.mock(ApplicationStartupEvent.class);
        zoneInitializer.onApplicationEvent(mockEvent);
        
        // Count entities in zone5
        int zone5Count = 0;
        for (String entityId : ecsWorld.getAllEntities()) {
            if (ecsWorld.getComponent(entityId, Zone.class)
                    .map(z -> z.zoneId().equals("zone5"))
                    .orElse(false)) {
                zone5Count++;
            }
        }
        
        // Zone5 should have monsters and at least one portal
        assertTrue(zone5Count > 0);
    }
    
    @Test
    void testAllZonesInitialized() {
        ApplicationStartupEvent mockEvent = Mockito.mock(ApplicationStartupEvent.class);
        zoneInitializer.onApplicationEvent(mockEvent);
        
        // Check that entities exist in all 5 zones using an array to make it effectively final
        final boolean[] hasZones = new boolean[5]; // [zone1, zone2, zone3, zone4, zone5]
        
        for (String entityId : ecsWorld.getAllEntities()) {
            ecsWorld.getComponent(entityId, Zone.class).ifPresent(zone -> {
                switch (zone.zoneId()) {
                    case "zone1": hasZones[0] = true; break;
                    case "zone2": hasZones[1] = true; break;
                    case "zone3": hasZones[2] = true; break;
                    case "zone4": hasZones[3] = true; break;
                    case "zone5": hasZones[4] = true; break;
                }
            });
        }
        
        assertTrue(hasZones[0], "Zone1 should be initialized");
        assertTrue(hasZones[1], "Zone2 should be initialized");
        assertTrue(hasZones[2], "Zone3 should be initialized");
        assertTrue(hasZones[3], "Zone4 should be initialized");
        assertTrue(hasZones[4], "Zone5 should be initialized");
    }
    
    @Test
    void testPortalsCreated() {
        ApplicationStartupEvent mockEvent = Mockito.mock(ApplicationStartupEvent.class);
        zoneInitializer.onApplicationEvent(mockEvent);
        
        // Count entities with Portal component
        int portalCount = 0;
        for (String entityId : ecsWorld.getAllEntities()) {
            if (ecsWorld.hasComponent(entityId, Portal.class)) {
                portalCount++;
            }
        }
        
        // Should have multiple portals connecting zones
        assertTrue(portalCount > 5);
    }
    
    @Test
    void testMonstersCreated() {
        ApplicationStartupEvent mockEvent = Mockito.mock(ApplicationStartupEvent.class);
        zoneInitializer.onApplicationEvent(mockEvent);
        
        // Count entities with Monster component
        int monsterCount = 0;
        for (String entityId : ecsWorld.getAllEntities()) {
            if (ecsWorld.hasComponent(entityId, Monster.class)) {
                monsterCount++;
            }
        }
        
        // Should have many monsters across all zones
        assertTrue(monsterCount > 10);
    }
    
    @Test
    void testNpcsCreated() {
        ApplicationStartupEvent mockEvent = Mockito.mock(ApplicationStartupEvent.class);
        zoneInitializer.onApplicationEvent(mockEvent);
        
        // Count entities with NpcAi.FRIENDLY_WANDER
        int npcCount = 0;
        for (String entityId : ecsWorld.getAllEntities()) {
            if (ecsWorld.getComponent(entityId, NpcAi.class)
                    .map(ai -> ai.type() == NpcAi.AiType.FRIENDLY_WANDER)
                    .orElse(false)) {
                npcCount++;
            }
        }
        
        // Should have friendly NPCs in zone1
        assertTrue(npcCount > 0);
    }
}
