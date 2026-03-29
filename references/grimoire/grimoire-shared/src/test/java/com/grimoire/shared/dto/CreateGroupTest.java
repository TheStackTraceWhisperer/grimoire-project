package com.grimoire.shared.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CreateGroupTest {

    @Test
    void testCreateGroupCreation() {
        CreateGroup createGroup = new CreateGroup("myGroup");
        
        assertEquals("myGroup", createGroup.groupName());
    }

    @Test
    void testCreateGroupIsSerializable() {
        CreateGroup createGroup = new CreateGroup("group");
        assertInstanceOf(java.io.Serializable.class, createGroup);
    }
}
