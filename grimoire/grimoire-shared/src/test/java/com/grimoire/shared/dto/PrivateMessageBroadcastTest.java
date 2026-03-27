package com.grimoire.shared.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PrivateMessageBroadcastTest {

    @Test
    void testPrivateMessageBroadcastCreation() {
        PrivateMessageBroadcast broadcast = new PrivateMessageBroadcast("sender", "Hello");
        
        assertEquals("sender", broadcast.sender());
        assertEquals("Hello", broadcast.message());
    }

    @Test
    void testPrivateMessageBroadcastIsSerializable() {
        PrivateMessageBroadcast broadcast = new PrivateMessageBroadcast("user", "msg");
        assertInstanceOf(java.io.Serializable.class, broadcast);
    }
}
