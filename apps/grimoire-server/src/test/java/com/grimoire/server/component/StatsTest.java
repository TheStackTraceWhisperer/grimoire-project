package com.grimoire.server.component;

import com.grimoire.ecs.Component;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StatsTest {
    
    @Test
    void testStatsCreation() {
        Stats stats = new Stats(50, 100, 5, 10);
        
        assertEquals(50, stats.hp());
        assertEquals(100, stats.maxHp());
        assertEquals(5, stats.defense());
        assertEquals(10, stats.attack());
    }
    
    @Test
    void testStatsIsComponent() {
        Stats stats = new Stats(1, 1, 0, 0);
        assertInstanceOf(Component.class, stats);
    }
}
