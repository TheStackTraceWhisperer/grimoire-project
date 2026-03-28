package com.grimoire.shared.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CharacterSelectionSuccessTest {

    @Test
    void testCharacterSelectionSuccessCreation() {
        CharacterSelectionSuccess success = new CharacterSelectionSuccess(
                "entity-123",
                "Hero",
                10,
                "zone1",
                100.5,
                200.3
        );
        
        assertEquals("entity-123", success.entityId());
        assertEquals("Hero", success.characterName());
        assertEquals(10, success.level());
        assertEquals("zone1", success.zone());
        assertEquals(100.5, success.x(), 0.001);
        assertEquals(200.3, success.y(), 0.001);
    }

    @Test
    void testCharacterSelectionSuccessIsSerializable() {
        CharacterSelectionSuccess success = new CharacterSelectionSuccess(
                "entity-1", "Hero", 1, "zone1", 0.0, 0.0
        );
        assertInstanceOf(java.io.Serializable.class, success);
    }
}
