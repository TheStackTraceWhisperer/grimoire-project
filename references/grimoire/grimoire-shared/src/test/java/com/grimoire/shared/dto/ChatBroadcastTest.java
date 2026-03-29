package com.grimoire.shared.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ChatBroadcastTest {

    @Test
    void testChatBroadcastCreation() {
        ChatBroadcast broadcast = new ChatBroadcast("testUser", "Hello World");
        
        assertEquals("testUser", broadcast.sender());
        assertEquals("Hello World", broadcast.message());
    }

    @Test
    void testChatBroadcastIsSerializable() {
        ChatBroadcast broadcast = new ChatBroadcast("user", "msg");
        assertInstanceOf(java.io.Serializable.class, broadcast);
    }
}
