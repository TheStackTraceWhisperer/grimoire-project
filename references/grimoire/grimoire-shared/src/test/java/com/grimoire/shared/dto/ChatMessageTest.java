package com.grimoire.shared.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ChatMessageTest {

    @Test
    void testChatMessageCreation() {
        ChatMessage message = new ChatMessage("Hello World");
        
        assertEquals("Hello World", message.message());
    }

    @Test
    void testChatMessageIsSerializable() {
        ChatMessage message = new ChatMessage("Test message");
        assertInstanceOf(java.io.Serializable.class, message);
    }
}
