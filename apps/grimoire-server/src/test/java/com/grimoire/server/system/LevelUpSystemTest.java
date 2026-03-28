package com.grimoire.server.system;

import com.grimoire.server.component.Dirty;
import com.grimoire.server.component.Experience;
import com.grimoire.server.component.Stats;
import com.grimoire.ecs.ComponentManager;
import com.grimoire.ecs.EcsWorld;
import com.grimoire.ecs.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LevelUpSystemTest {
    
    private EcsWorld ecsWorld;
    private LevelUpSystem levelUpSystem;
    
    @BeforeEach
    void setUp() {
        EntityManager entityManager = new EntityManager();
        ComponentManager componentManager = new ComponentManager();
        ecsWorld = new EcsWorld(entityManager, componentManager);
        levelUpSystem = new LevelUpSystem(ecsWorld);
    }
    
    @Test
    void testNoLevelUpWhenXpBelowThreshold() {
        String entityId = ecsWorld.createEntity();
        ecsWorld.addComponent(entityId, new Experience(50, 100));
        ecsWorld.addComponent(entityId, new Stats(100, 100, 10, 10));
        
        levelUpSystem.tick(0.05f);
        
        Experience exp = ecsWorld.getComponent(entityId, Experience.class).orElseThrow();
        assertEquals(50, exp.currentXp());
        assertEquals(100, exp.xpToNextLevel());
    }
    
    @Test
    void testLevelUpWhenXpEqualsThreshold() {
        String entityId = ecsWorld.createEntity();
        ecsWorld.addComponent(entityId, new Experience(100, 100));
        ecsWorld.addComponent(entityId, new Stats(100, 100, 10, 10));
        
        levelUpSystem.tick(0.05f);
        
        Experience exp = ecsWorld.getComponent(entityId, Experience.class).orElseThrow();
        assertEquals(0, exp.currentXp());
        assertEquals(150, exp.xpToNextLevel()); // 100 * 1.5
    }
    
    @Test
    void testLevelUpWithExcessXp() {
        String entityId = ecsWorld.createEntity();
        ecsWorld.addComponent(entityId, new Experience(120, 100));
        ecsWorld.addComponent(entityId, new Stats(100, 100, 10, 10));
        
        levelUpSystem.tick(0.05f);
        
        Experience exp = ecsWorld.getComponent(entityId, Experience.class).orElseThrow();
        assertEquals(20, exp.currentXp()); // 120 - 100 = 20 remaining
        assertEquals(150, exp.xpToNextLevel());
    }
    
    @Test
    void testMultipleLevelUpsInOneTick() {
        String entityId = ecsWorld.createEntity();
        // 250 XP with 100 threshold -> level up (150 left, threshold 150)
        // -> level up again (0 left, threshold 225)
        ecsWorld.addComponent(entityId, new Experience(250, 100));
        ecsWorld.addComponent(entityId, new Stats(100, 100, 10, 10));
        
        levelUpSystem.tick(0.05f);
        
        Experience exp = ecsWorld.getComponent(entityId, Experience.class).orElseThrow();
        assertEquals(0, exp.currentXp());
        assertEquals(225, exp.xpToNextLevel()); // 100 * 1.5 * 1.5 = 225
    }
    
    @Test
    void testStatsIncreasedOnLevelUp() {
        String entityId = ecsWorld.createEntity();
        ecsWorld.addComponent(entityId, new Experience(100, 100));
        ecsWorld.addComponent(entityId, new Stats(100, 100, 10, 10));
        
        levelUpSystem.tick(0.05f);
        
        Stats stats = ecsWorld.getComponent(entityId, Stats.class).orElseThrow();
        assertEquals(110, stats.maxHp()); // +10 per level
        assertEquals(110, stats.hp());    // Heals gained HP
        assertEquals(12, stats.attack()); // +2 per level
        assertEquals(11, stats.defense()); // +1 per level
    }
    
    @Test
    void testDirtyFlagSetOnLevelUp() {
        String entityId = ecsWorld.createEntity();
        ecsWorld.addComponent(entityId, new Experience(100, 100));
        ecsWorld.addComponent(entityId, new Stats(100, 100, 10, 10));
        
        levelUpSystem.tick(0.05f);
        
        assertTrue(ecsWorld.hasComponent(entityId, Dirty.class));
    }
    
    @Test
    void testEntityWithoutExperienceComponentIgnored() {
        String entityId = ecsWorld.createEntity();
        ecsWorld.addComponent(entityId, new Stats(100, 100, 10, 10));
        
        // Should not throw
        levelUpSystem.tick(0.05f);
        
        assertFalse(ecsWorld.hasComponent(entityId, Experience.class));
    }
}
