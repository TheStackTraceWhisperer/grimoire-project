package com.grimoire.server.component;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for SpawnPoint component.
 */
class SpawnPointTest {
    
    @Test
    void testSpawnPointCreation() {
        SpawnPoint spawn = new SpawnPoint(100.0, 200.0, 150.0);
        
        assertEquals(100.0, spawn.x(), 0.001);
        assertEquals(200.0, spawn.y(), 0.001);
        assertEquals(150.0, spawn.leashRadius(), 0.001);
    }
    
    @Test
    void testSpawnPointDefaultValues() {
        SpawnPoint spawn = new SpawnPoint(0.0, 0.0, 0.0);
        
        assertEquals(0.0, spawn.x(), 0.001);
        assertEquals(0.0, spawn.y(), 0.001);
        assertEquals(0.0, spawn.leashRadius(), 0.001);
    }
    
    @Test
    void testSpawnPointImmutability() {
        SpawnPoint spawn1 = new SpawnPoint(100.0, 200.0, 150.0);
        SpawnPoint spawn2 = new SpawnPoint(50.0, 75.0, 100.0);
        
        // Verify they are different instances
        assertNotEquals(spawn1, spawn2);
        assertEquals(100.0, spawn1.x(), 0.001);
        assertEquals(50.0, spawn2.x(), 0.001);
    }
}
