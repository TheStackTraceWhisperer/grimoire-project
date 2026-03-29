package com.grimoire.server.content;

import com.grimoire.server.component.*;
import com.grimoire.server.ecs.ComponentManager;
import com.grimoire.server.ecs.EcsWorld;
import com.grimoire.server.ecs.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NpcFactoryTest {
    
    private NpcFactory npcFactory;
    private EcsWorld ecsWorld;
    private PrefabRegistry prefabRegistry;
    
    @BeforeEach
    void setUp() {
        EntityManager entityManager = new EntityManager();
        ComponentManager componentManager = new ComponentManager();
        ecsWorld = new EcsWorld(entityManager, componentManager);
        prefabRegistry = new PrefabRegistry(ecsWorld);
        prefabRegistry.onApplicationEvent(null); // Register prefabs
        npcFactory = new NpcFactory(ecsWorld);
    }
    
    @Test
    void testCreateRat() {
        String entityId = npcFactory.createRat("zone1", 100, 200);
        
        assertNotNull(entityId);
        assertTrue(ecsWorld.entityExists(entityId));
        
        // Verify rat has expected components
        assertTrue(ecsWorld.hasComponent(entityId, Zone.class));
        assertTrue(ecsWorld.hasComponent(entityId, Position.class));
        assertTrue(ecsWorld.hasComponent(entityId, Velocity.class));
        assertTrue(ecsWorld.hasComponent(entityId, Renderable.class));
        assertTrue(ecsWorld.hasComponent(entityId, Stats.class));
        assertTrue(ecsWorld.hasComponent(entityId, Monster.class));
        assertTrue(ecsWorld.hasComponent(entityId, NpcAi.class));
        assertTrue(ecsWorld.hasComponent(entityId, BoundingBox.class));
        
        // Verify specific values
        assertEquals("zone1", ecsWorld.getComponent(entityId, Zone.class).get().zoneId());
        assertEquals(100, ecsWorld.getComponent(entityId, Position.class).get().x(), 0.001);
        assertEquals(200, ecsWorld.getComponent(entityId, Position.class).get().y(), 0.001);
        assertEquals(Monster.MonsterType.RAT, ecsWorld.getComponent(entityId, Monster.class).get().type());
        assertEquals("Rat", ecsWorld.getComponent(entityId, Renderable.class).get().name());
    }
    
    @Test
    void testCreateWolf() {
        String entityId = npcFactory.createWolf("zone2", 150, 250);
        
        assertNotNull(entityId);
        assertTrue(ecsWorld.entityExists(entityId));
        assertTrue(ecsWorld.hasComponent(entityId, Monster.class));
        
        assertEquals(Monster.MonsterType.WOLF, ecsWorld.getComponent(entityId, Monster.class).get().type());
        assertEquals("Wolf", ecsWorld.getComponent(entityId, Renderable.class).get().name());
        assertEquals(40, ecsWorld.getComponent(entityId, Stats.class).get().hp());
    }
    
    @Test
    void testCreateBat() {
        String entityId = npcFactory.createBat("zone3", 175, 225);
        
        assertNotNull(entityId);
        assertTrue(ecsWorld.entityExists(entityId));
        assertTrue(ecsWorld.hasComponent(entityId, Monster.class));
        
        assertEquals(Monster.MonsterType.BAT, ecsWorld.getComponent(entityId, Monster.class).get().type());
        assertEquals("Bat", ecsWorld.getComponent(entityId, Renderable.class).get().name());
        assertEquals(15, ecsWorld.getComponent(entityId, Stats.class).get().hp());
    }
    
    @Test
    void testCreateSkeleton() {
        String entityId = npcFactory.createSkeleton("zone4", 200, 300);
        
        assertNotNull(entityId);
        assertTrue(ecsWorld.entityExists(entityId));
        assertTrue(ecsWorld.hasComponent(entityId, Monster.class));
        
        assertEquals(Monster.MonsterType.SKELETON, ecsWorld.getComponent(entityId, Monster.class).get().type());
        assertEquals("Skeleton", ecsWorld.getComponent(entityId, Renderable.class).get().name());
        assertEquals(60, ecsWorld.getComponent(entityId, Stats.class).get().hp());
    }
    
    @Test
    void testCreateFriendlyNpc() {
        String entityId = npcFactory.createFriendlyNpc("zone1", 100, 150, "Village Elder");
        
        assertNotNull(entityId);
        assertTrue(ecsWorld.entityExists(entityId));
        assertTrue(ecsWorld.hasComponent(entityId, NpcAi.class));
        
        assertEquals(NpcAi.AiType.FRIENDLY_WANDER, ecsWorld.getComponent(entityId, NpcAi.class).get().type());
        assertEquals("Village Elder", ecsWorld.getComponent(entityId, Renderable.class).get().name());
        assertFalse(ecsWorld.hasComponent(entityId, Monster.class));
    }
    
    @Test
    void testCreatePortal() {
        String entityId = npcFactory.createPortal("zone1", 500, 250, "portal-1", "zone2", "portal-2");
        
        assertNotNull(entityId);
        assertTrue(ecsWorld.entityExists(entityId));
        assertTrue(ecsWorld.hasComponent(entityId, Portal.class));
        assertTrue(ecsWorld.hasComponent(entityId, Position.class));
        assertTrue(ecsWorld.hasComponent(entityId, Renderable.class));
        assertTrue(ecsWorld.hasComponent(entityId, BoundingBox.class));
        
        Portal portal = ecsWorld.getComponent(entityId, Portal.class).get();
        assertEquals("zone2", portal.targetZoneId());
        assertEquals("portal-2", portal.targetPortalId());
        
        Renderable renderable = ecsWorld.getComponent(entityId, Renderable.class).get();
        assertEquals("portal-1", renderable.name());
    }
}
