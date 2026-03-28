package com.grimoire.shared.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EntityDespawnTest {

    @Test
    void testEntityDespawnCreation() {
        EntityDespawn despawn = new EntityDespawn("entity-123");
        
        assertEquals("entity-123", despawn.entityId());
    }

    @Test
    void testEntityDespawnIsSerializable() {
        EntityDespawn despawn = new EntityDespawn("entity-456");
        assertInstanceOf(java.io.Serializable.class, despawn);
    }
}
