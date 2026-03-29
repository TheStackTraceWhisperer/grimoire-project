package com.grimoire.shared.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PrivateMessageTest {

    @Test
    void testPrivateMessageCreation() {
        PrivateMessage message = new PrivateMessage("recipient", "Hello");
        
        assertEquals("recipient", message.recipientName());
        assertEquals("Hello", message.message());
    }

    @Test
    void testPrivateMessageIsSerializable() {
        PrivateMessage message = new PrivateMessage("user", "msg");
        assertInstanceOf(java.io.Serializable.class, message);
    }
}
