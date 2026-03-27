package com.grimoire.shared.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GroupMessageTest {

    @Test
    void testGroupMessageCreation() {
        GroupMessage message = new GroupMessage("myGroup", "Hello group");
        
        assertEquals("myGroup", message.groupName());
        assertEquals("Hello group", message.message());
    }

    @Test
    void testGroupMessageIsSerializable() {
        GroupMessage message = new GroupMessage("group", "msg");
        assertInstanceOf(java.io.Serializable.class, message);
    }
}
