package com.grimoire.data;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class CharacterTest {

    @Test
    void testNoArgsConstructorInitializesDefaults() {
        Character character = new Character();

        assertEquals(1, character.getLevel());
        assertEquals(100.0, character.getLastX());
        assertEquals(100.0, character.getLastY());
        assertEquals("zone1", character.getLastZone());
        assertEquals(100, character.getCurrentHp());
        assertEquals(100, character.getMaxHp());
        assertEquals(0, character.getCurrentXp());
        assertEquals(100, character.getXpToNextLevel());
        assertNotNull(character.getCreatedAt());
        assertNotNull(character.getLastPlayedAt());
    }

    @Test
    void testNameAndAccountConstructorSetsCoreFields() {
        Account account = new Account("owner");
        Character character = new Character("Hero", account);

        assertEquals("Hero", character.getName());
        assertEquals(account, character.getAccount());
        assertNotNull(character.getCreatedAt());
        assertNotNull(character.getLastPlayedAt());
    }

    @Test
    void testSettersAndGettersRoundTrip() {
        Account account = new Account("owner");
        Character character = new Character();
        LocalDateTime createdAt = LocalDateTime.now().minusDays(2);
        LocalDateTime lastPlayedAt = LocalDateTime.now().minusHours(3);

        character.setId(7L);
        character.setName("SetterHero");
        character.setAccount(account);
        character.setLevel(9);
        character.setLastX(12.5);
        character.setLastY(98.75);
        character.setLastZone("zone9");
        character.setCurrentHp(65);
        character.setMaxHp(120);
        character.setCurrentXp(340);
        character.setXpToNextLevel(500);
        character.setCreatedAt(createdAt);
        character.setLastPlayedAt(lastPlayedAt);

        assertEquals(7L, character.getId());
        assertEquals("SetterHero", character.getName());
        assertEquals(account, character.getAccount());
        assertEquals(9, character.getLevel());
        assertEquals(12.5, character.getLastX());
        assertEquals(98.75, character.getLastY());
        assertEquals("zone9", character.getLastZone());
        assertEquals(65, character.getCurrentHp());
        assertEquals(120, character.getMaxHp());
        assertEquals(340, character.getCurrentXp());
        assertEquals(500, character.getXpToNextLevel());
        assertEquals(createdAt, character.getCreatedAt());
        assertEquals(lastPlayedAt, character.getLastPlayedAt());
    }
}

