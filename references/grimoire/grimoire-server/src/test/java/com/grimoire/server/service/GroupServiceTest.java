package com.grimoire.server.service;

import com.grimoire.data.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class GroupServiceTest {

    @Mock
    private PlayerGroupRepository groupRepository;

    @Mock
    private GroupMembershipRepository membershipRepository;

    @Mock
    private AccountRepository accountRepository;

    private GroupService groupService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        groupService = new GroupService(groupRepository, membershipRepository, accountRepository);
    }

    @Test
    void testCreateGroup_Success() {
        Account owner = new Account("testUser");
        owner.setId(1L);

        when(groupRepository.findByName("TestGroup")).thenReturn(Optional.empty());
        when(accountRepository.findById(1L)).thenReturn(Optional.of(owner));
        
        PlayerGroup savedGroup = new PlayerGroup("TestGroup", owner);
        savedGroup.setId(1L);
        when(groupRepository.save(any(PlayerGroup.class))).thenReturn(savedGroup);
        when(membershipRepository.save(any(GroupMembership.class))).thenReturn(new GroupMembership());

        Optional<PlayerGroup> result = groupService.createGroup("TestGroup", 1L);

        assertTrue(result.isPresent());
        assertEquals("TestGroup", result.get().getName());
        verify(groupRepository).save(any(PlayerGroup.class));
        verify(membershipRepository).save(any(GroupMembership.class));
    }

    @Test
    void testCreateGroup_DuplicateName() {
        PlayerGroup existingGroup = new PlayerGroup("TestGroup", new Account("other"));
        when(groupRepository.findByName("TestGroup")).thenReturn(Optional.of(existingGroup));

        Optional<PlayerGroup> result = groupService.createGroup("TestGroup", 1L);

        assertFalse(result.isPresent());
        verify(groupRepository, never()).save(any(PlayerGroup.class));
    }

    @Test
    void testJoinGroup_Success() {
        Account owner = new Account("owner");
        owner.setId(1L);
        
        PlayerGroup group = new PlayerGroup("TestGroup", owner);
        group.setId(1L);
        
        Account member = new Account("member");
        member.setId(2L);

        when(groupRepository.findByName("TestGroup")).thenReturn(Optional.of(group));
        when(accountRepository.findById(2L)).thenReturn(Optional.of(member));
        when(membershipRepository.findByGroupIdAndAccountId(1L, 2L)).thenReturn(Optional.empty());
        when(membershipRepository.save(any(GroupMembership.class))).thenReturn(new GroupMembership());

        boolean result = groupService.joinGroup("TestGroup", 2L);

        assertTrue(result);
        verify(membershipRepository).save(any(GroupMembership.class));
    }

    @Test
    void testJoinGroup_AlreadyMember() {
        Account owner = new Account("owner");
        owner.setId(1L);
        
        PlayerGroup group = new PlayerGroup("TestGroup", owner);
        group.setId(1L);
        
        Account member = new Account("member");
        member.setId(2L);

        when(groupRepository.findByName("TestGroup")).thenReturn(Optional.of(group));
        when(accountRepository.findById(2L)).thenReturn(Optional.of(member));
        when(membershipRepository.findByGroupIdAndAccountId(1L, 2L))
                .thenReturn(Optional.of(new GroupMembership(group, member)));

        boolean result = groupService.joinGroup("TestGroup", 2L);

        assertFalse(result);
        verify(membershipRepository, never()).save(any(GroupMembership.class));
    }

    @Test
    void testLeaveGroup_Success() {
        Account owner = new Account("owner");
        owner.setId(1L);
        
        PlayerGroup group = new PlayerGroup("TestGroup", owner);
        group.setId(1L);

        when(groupRepository.findByName("TestGroup")).thenReturn(Optional.of(group));
        when(membershipRepository.findByGroupIdAndAccountId(1L, 2L))
                .thenReturn(Optional.of(new GroupMembership()));

        boolean result = groupService.leaveGroup("TestGroup", 2L);

        assertTrue(result);
        verify(membershipRepository).deleteByGroupIdAndAccountId(1L, 2L);
    }

    @Test
    void testLeaveGroup_NotMember() {
        Account owner = new Account("owner");
        owner.setId(1L);
        
        PlayerGroup group = new PlayerGroup("TestGroup", owner);
        group.setId(1L);

        when(groupRepository.findByName("TestGroup")).thenReturn(Optional.of(group));
        when(membershipRepository.findByGroupIdAndAccountId(1L, 2L)).thenReturn(Optional.empty());

        boolean result = groupService.leaveGroup("TestGroup", 2L);

        assertFalse(result);
        verify(membershipRepository, never()).deleteByGroupIdAndAccountId(anyLong(), anyLong());
    }

    @Test
    void testGetGroupMembers() {
        Account owner = new Account("owner");
        owner.setId(1L);
        
        Account member1 = new Account("member1");
        member1.setId(2L);
        
        Account member2 = new Account("member2");
        member2.setId(3L);
        
        PlayerGroup group = new PlayerGroup("TestGroup", owner);
        group.setId(1L);
        
        GroupMembership membership1 = new GroupMembership(group, owner);
        GroupMembership membership2 = new GroupMembership(group, member1);
        GroupMembership membership3 = new GroupMembership(group, member2);

        when(groupRepository.findByName("TestGroup")).thenReturn(Optional.of(group));
        when(membershipRepository.findByGroupId(1L))
                .thenReturn(List.of(membership1, membership2, membership3));

        List<Account> members = groupService.getGroupMembers("TestGroup");

        assertEquals(3, members.size());
        assertTrue(members.contains(owner));
        assertTrue(members.contains(member1));
        assertTrue(members.contains(member2));
    }

    @Test
    void testIsMemberOfGroup_True() {
        Account owner = new Account("owner");
        owner.setId(1L);
        
        PlayerGroup group = new PlayerGroup("TestGroup", owner);
        group.setId(1L);

        when(groupRepository.findByName("TestGroup")).thenReturn(Optional.of(group));
        when(membershipRepository.findByGroupIdAndAccountId(1L, 1L))
                .thenReturn(Optional.of(new GroupMembership()));

        boolean result = groupService.isMemberOfGroup("TestGroup", 1L);

        assertTrue(result);
    }

    @Test
    void testIsMemberOfGroup_False() {
        Account owner = new Account("owner");
        owner.setId(1L);
        
        PlayerGroup group = new PlayerGroup("TestGroup", owner);
        group.setId(1L);

        when(groupRepository.findByName("TestGroup")).thenReturn(Optional.of(group));
        when(membershipRepository.findByGroupIdAndAccountId(1L, 2L)).thenReturn(Optional.empty());

        boolean result = groupService.isMemberOfGroup("TestGroup", 2L);

        assertFalse(result);
    }
}
