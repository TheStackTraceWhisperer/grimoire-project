package com.grimoire.shared.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CharacterSelectionRequestTest {

    @Test
    void testCharacterSelectionRequestCreation() {
        CharacterSelectionRequest request = new CharacterSelectionRequest(42L);
        
        assertEquals(42L, request.characterId());
    }

    @Test
    void testCharacterSelectionRequestIsSerializable() {
        CharacterSelectionRequest request = new CharacterSelectionRequest(1L);
        assertInstanceOf(java.io.Serializable.class, request);
    }
}
