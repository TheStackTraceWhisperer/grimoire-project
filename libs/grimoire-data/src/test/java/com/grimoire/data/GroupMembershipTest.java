package com.grimoire.data;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class GroupMembershipTest {

    @Test
    void testGroupMembershipCreation() {
        Account owner = new Account("owner");
        owner.setId(1L);
        
        PlayerGroup group = new PlayerGroup("TestGroup", owner);
        group.setId(1L);
        
        Account member = new Account("member");
        member.setId(2L);
        
        GroupMembership membership = new GroupMembership(group, member);
        
        assertEquals(group, membership.getGroup());
        assertEquals(member, membership.getAccount());
        assertNotNull(membership.getJoinedAt());
    }

    @Test
    void testGroupMembershipSetters() {
        Account owner = new Account("owner");
        owner.setId(1L);
        
        PlayerGroup group = new PlayerGroup("TestGroup", owner);
        group.setId(1L);
        
        Account member = new Account("member");
        member.setId(2L);
        
        GroupMembership membership = new GroupMembership();
        membership.setGroup(group);
        membership.setAccount(member);
        
        LocalDateTime now = LocalDateTime.now();
        membership.setJoinedAt(now);
        
        assertEquals(group, membership.getGroup());
        assertEquals(member, membership.getAccount());
        assertEquals(now, membership.getJoinedAt());
    }
}
