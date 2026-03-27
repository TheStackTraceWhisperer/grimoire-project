package com.grimoire.data;

import io.micronaut.data.annotation.Repository;
import io.micronaut.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for PlayerGroup entities.
 */
@Repository
public interface PlayerGroupRepository extends JpaRepository<PlayerGroup, Long> {
    
    /**
     * Find a group by its name.
     */
    Optional<PlayerGroup> findByName(String name);
    
    /**
     * Find all groups owned by a specific account.
     */
    List<PlayerGroup> findByOwnerId(Long ownerId);
}
