package com.grimoire.server.service;

import com.grimoire.data.*;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service for managing player groups.
 * Handles group creation, membership management, and message routing.
 */
@Singleton
@RequiredArgsConstructor
@Slf4j
public class GroupService {
    
    private final PlayerGroupRepository groupRepository;
    private final GroupMembershipRepository membershipRepository;
    private final AccountRepository accountRepository;
    
    /**
     * Create a new group with the given name and owner.
     */
    public Optional<PlayerGroup> createGroup(String groupName, Long ownerAccountId) {
        // Check if group name already exists
        if (groupRepository.findByName(groupName).isPresent()) {
            log.warn("Group with name {} already exists", groupName);
            return Optional.empty();
        }
        
        Optional<Account> ownerOpt = accountRepository.findById(ownerAccountId);
        if (ownerOpt.isEmpty()) {
            log.warn("Account {} not found", ownerAccountId);
            return Optional.empty();
        }
        
        Account owner = ownerOpt.get();
        PlayerGroup group = new PlayerGroup(groupName, owner);
        group = groupRepository.save(group);
        
        // Automatically add the owner as a member
        GroupMembership membership = new GroupMembership(group, owner);
        membershipRepository.save(membership);
        
        log.info("Created group {} with owner {}", groupName, owner.getUsername());
        return Optional.of(group);
    }
    
    /**
     * Add a member to a group.
     */
    public boolean joinGroup(String groupName, Long accountId) {
        Optional<PlayerGroup> groupOpt = groupRepository.findByName(groupName);
        if (groupOpt.isEmpty()) {
            log.warn("Group {} not found", groupName);
            return false;
        }
        
        Optional<Account> accountOpt = accountRepository.findById(accountId);
        if (accountOpt.isEmpty()) {
            log.warn("Account {} not found", accountId);
            return false;
        }
        
        PlayerGroup group = groupOpt.get();
        Account account = accountOpt.get();
        
        // Check if already a member
        if (membershipRepository.findByGroupIdAndAccountId(group.getId(), accountId).isPresent()) {
            log.warn("Account {} is already a member of group {}", accountId, groupName);
            return false;
        }
        
        GroupMembership membership = new GroupMembership(group, account);
        membershipRepository.save(membership);
        
        log.info("Account {} joined group {}", account.getUsername(), groupName);
        return true;
    }
    
    /**
     * Remove a member from a group.
     */
    public boolean leaveGroup(String groupName, Long accountId) {
        Optional<PlayerGroup> groupOpt = groupRepository.findByName(groupName);
        if (groupOpt.isEmpty()) {
            log.warn("Group {} not found", groupName);
            return false;
        }
        
        PlayerGroup group = groupOpt.get();
        
        // Check if member exists
        if (membershipRepository.findByGroupIdAndAccountId(group.getId(), accountId).isEmpty()) {
            log.warn("Account {} is not a member of group {}", accountId, groupName);
            return false;
        }
        
        membershipRepository.deleteByGroupIdAndAccountId(group.getId(), accountId);
        
        log.info("Account {} left group {}", accountId, groupName);
        return true;
    }
    
    /**
     * Get all members of a group.
     */
    public List<Account> getGroupMembers(String groupName) {
        Optional<PlayerGroup> groupOpt = groupRepository.findByName(groupName);
        if (groupOpt.isEmpty()) {
            return List.of();
        }
        
        return membershipRepository.findByGroupId(groupOpt.get().getId())
                .stream()
                .map(GroupMembership::getAccount)
                .collect(Collectors.toList());
    }
    
    /**
     * Get all groups for an account.
     */
    public List<PlayerGroup> getGroupsForAccount(Long accountId) {
        return membershipRepository.findByAccountId(accountId)
                .stream()
                .map(GroupMembership::getGroup)
                .collect(Collectors.toList());
    }
    
    /**
     * Check if an account is a member of a group.
     */
    public boolean isMemberOfGroup(String groupName, Long accountId) {
        Optional<PlayerGroup> groupOpt = groupRepository.findByName(groupName);
        if (groupOpt.isEmpty()) {
            return false;
        }
        
        return membershipRepository.findByGroupIdAndAccountId(groupOpt.get().getId(), accountId).isPresent();
    }
}
