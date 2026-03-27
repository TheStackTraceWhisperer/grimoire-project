package com.grimoire.shared.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ZoneChangeTest {

    @Test
    void testZoneChangeCreation() {
        ZoneChange change = new ZoneChange("zone-2", 100.0, 200.0);
        
        assertEquals("zone-2", change.newZoneId());
        assertEquals(100.0, change.spawnX(), 0.001);
        assertEquals(200.0, change.spawnY(), 0.001);
    }

    @Test
    void testZoneChangeIsSerializable() {
        ZoneChange change = new ZoneChange("zone-1", 0, 0);
        assertInstanceOf(java.io.Serializable.class, change);
    }
}
