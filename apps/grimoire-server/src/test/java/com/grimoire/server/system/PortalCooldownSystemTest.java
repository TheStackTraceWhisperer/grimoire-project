package com.grimoire.server.system;

import com.grimoire.server.component.PortalCooldown;
import com.grimoire.ecs.ComponentManager;
import com.grimoire.ecs.EntityManager;
import com.grimoire.ecs.EcsWorld;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PortalCooldownSystemTest {
    
    @Test
    void testCooldownDecrement() {
        EntityManager entityManager = new EntityManager();
        ComponentManager componentManager = new ComponentManager();
        EcsWorld ecsWorld = new EcsWorld(entityManager, componentManager);
        PortalCooldownSystem system = new PortalCooldownSystem(ecsWorld);
        
        String entityId = ecsWorld.createEntity();
        ecsWorld.addComponent(entityId, new PortalCooldown(5));
        
        system.tick(0.05f);
        
        var cooldown = ecsWorld.getComponent(entityId, PortalCooldown.class);
        assertTrue(cooldown.isPresent());
        assertEquals(4, cooldown.get().ticksRemaining());
    }
    
    @Test
    void testCooldownRemoval() {
        EntityManager entityManager = new EntityManager();
        ComponentManager componentManager = new ComponentManager();
        EcsWorld ecsWorld = new EcsWorld(entityManager, componentManager);
        PortalCooldownSystem system = new PortalCooldownSystem(ecsWorld);
        
        String entityId = ecsWorld.createEntity();
        ecsWorld.addComponent(entityId, new PortalCooldown(1));
        
        system.tick(0.05f);
        
        assertFalse(ecsWorld.hasComponent(entityId, PortalCooldown.class));
    }
    
    @Test
    void testZeroCooldownRemoval() {
        EntityManager entityManager = new EntityManager();
        ComponentManager componentManager = new ComponentManager();
        EcsWorld ecsWorld = new EcsWorld(entityManager, componentManager);
        PortalCooldownSystem system = new PortalCooldownSystem(ecsWorld);
        
        String entityId = ecsWorld.createEntity();
        ecsWorld.addComponent(entityId, new PortalCooldown(0));
        
        system.tick(0.05f);
        
        assertFalse(ecsWorld.hasComponent(entityId, PortalCooldown.class));
    }
}
