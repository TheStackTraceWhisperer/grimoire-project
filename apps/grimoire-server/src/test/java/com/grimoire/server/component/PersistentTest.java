package com.grimoire.server.component;

import com.grimoire.ecs.Component;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PersistentTest {
    
    @Test
    void testPersistentCreation() {
        Persistent persistent = new Persistent("account-123");
        
        assertEquals("account-123", persistent.accountId());
    }
    
    @Test
    void testPersistentIsComponent() {
        Persistent persistent = new Persistent("account-1");
        assertInstanceOf(Component.class, persistent);
    }
    
    @Test
    void testPersistentWithEmptyId() {
        Persistent persistent = new Persistent("");
        assertEquals("", persistent.accountId());
    }
}
