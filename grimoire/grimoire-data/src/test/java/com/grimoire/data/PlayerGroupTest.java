package com.grimoire.data;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class PlayerGroupTest {

    @Test
    void testPlayerGroupCreation() {
        Account owner = new Account("testUser");
        owner.setId(1L);
        
        PlayerGroup group = new PlayerGroup("TestGroup", owner);
        
        assertEquals("TestGroup", group.getName());
        assertEquals(owner, group.getOwner());
        assertNotNull(group.getCreatedAt());
        assertNotNull(group.getMemberships());
        assertTrue(group.getMemberships().isEmpty());
    }

    @Test
    void testPlayerGroupSetters() {
        Account owner = new Account("owner");
        owner.setId(1L);
        
        PlayerGroup group = new PlayerGroup();
        group.setName("MyGroup");
        group.setOwner(owner);
        
        LocalDateTime now = LocalDateTime.now();
        group.setCreatedAt(now);
        
        assertEquals("MyGroup", group.getName());
        assertEquals(owner, group.getOwner());
        assertEquals(now, group.getCreatedAt());
    }
}
