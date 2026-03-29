package com.grimoire.shared.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GroupMessageBroadcastTest {

    @Test
    void testGroupMessageBroadcastCreation() {
        GroupMessageBroadcast broadcast = new GroupMessageBroadcast("myGroup", "sender", "Hello");
        
        assertEquals("myGroup", broadcast.groupName());
        assertEquals("sender", broadcast.sender());
        assertEquals("Hello", broadcast.message());
    }

    @Test
    void testGroupMessageBroadcastIsSerializable() {
        GroupMessageBroadcast broadcast = new GroupMessageBroadcast("group", "user", "msg");
        assertInstanceOf(java.io.Serializable.class, broadcast);
    }
}
