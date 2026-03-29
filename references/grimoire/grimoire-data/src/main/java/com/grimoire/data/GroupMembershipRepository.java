package com.grimoire.data;

import io.micronaut.data.annotation.Repository;
import io.micronaut.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for GroupMembership entities.
 */
@Repository
public interface GroupMembershipRepository extends JpaRepository<GroupMembership, Long> {
    
    /**
     * Find all memberships for a specific group.
     */
    List<GroupMembership> findByGroupId(Long groupId);
    
    /**
     * Find all memberships for a specific account.
     */
    List<GroupMembership> findByAccountId(Long accountId);
    
    /**
     * Find a specific membership by group and account.
     */
    Optional<GroupMembership> findByGroupIdAndAccountId(Long groupId, Long accountId);
    
    /**
     * Delete a membership by group and account.
     */
    void deleteByGroupIdAndAccountId(Long groupId, Long accountId);
}
