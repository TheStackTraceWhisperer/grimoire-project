package com.grimoire.data;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AccountTest {

    @Test
    void testNoArgsConstructorInitializesDefaults() {
        Account account = new Account();

        assertNotNull(account.getCreatedAt());
        assertNotNull(account.getCharacters());
        assertTrue(account.getCharacters().isEmpty());
    }

    @Test
    void testUsernameConstructorSetsUsernameAndTimestamps() {
        Account account = new Account("test-user");

        assertEquals("test-user", account.getUsername());
        assertNotNull(account.getCreatedAt());
        assertNotNull(account.getCharacters());
        assertTrue(account.getCharacters().isEmpty());
    }

    @Test
    void testSettersAndGettersRoundTrip() {
        Account account = new Account();
        LocalDateTime createdAt = LocalDateTime.now().minusDays(1);
        List<Character> characters = new ArrayList<>();

        account.setId(42L);
        account.setUsername("setter-user");
        account.setCreatedAt(createdAt);
        account.setCharacters(characters);

        assertEquals(42L, account.getId());
        assertEquals("setter-user", account.getUsername());
        assertEquals(createdAt, account.getCreatedAt());
        assertEquals(characters, account.getCharacters());
    }
}

