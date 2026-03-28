package com.grimoire.shared.dto;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CharacterListResponseTest {

    @Test
    void testCharacterListResponseCreation() {
        CharacterListResponse.CharacterInfo char1 = new CharacterListResponse.CharacterInfo(1L, "Hero1", 10, "zone1");
        CharacterListResponse.CharacterInfo char2 = new CharacterListResponse.CharacterInfo(2L, "Hero2", 5, "zone2");
        List<CharacterListResponse.CharacterInfo> characters = Arrays.asList(char1, char2);
        
        CharacterListResponse response = new CharacterListResponse("session123", characters);
        
        assertEquals("session123", response.sessionId());
        assertEquals(2, response.characters().size());
        assertEquals("Hero1", response.characters().get(0).name());
        assertEquals(10, response.characters().get(0).level());
    }

    @Test
    void testCharacterListResponseIsSerializable() {
        CharacterListResponse response = new CharacterListResponse("session123", List.of());
        assertInstanceOf(java.io.Serializable.class, response);
    }

    @Test
    void testCharacterListResponseDefensivelyCopiesCharacters() {
        List<CharacterListResponse.CharacterInfo> source = new ArrayList<>();
        source.add(new CharacterListResponse.CharacterInfo(1L, "Hero1", 10, "zone1"));

        CharacterListResponse response = new CharacterListResponse("session123", source);
        source.clear();

        assertEquals(1, response.characters().size());
        assertThrows(UnsupportedOperationException.class,
                () -> response.characters().add(new CharacterListResponse.CharacterInfo(2L, "Hero2", 12, "zone2")));
    }

    @Test
    void testCharacterInfoCreation() {
        CharacterListResponse.CharacterInfo characterInfo = new CharacterListResponse.CharacterInfo(1L, "Hero", 10, "zone1");
        
        assertEquals(1L, characterInfo.id());
        assertEquals("Hero", characterInfo.name());
        assertEquals(10, characterInfo.level());
        assertEquals("zone1", characterInfo.lastZone());
    }

    @Test
    void testCharacterInfoIsSerializable() {
        CharacterListResponse.CharacterInfo characterInfo = new CharacterListResponse.CharacterInfo(1L, "Hero", 10, "zone1");
        assertInstanceOf(java.io.Serializable.class, characterInfo);
    }
}
