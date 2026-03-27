package com.grimoire.data;

import io.micronaut.data.annotation.Repository;
import io.micronaut.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repository for Character entities.
 */
@Repository
public interface CharacterRepository extends JpaRepository<Character, Long> {
    
    /**
     * Finds all characters for a given account.
     * @param accountId the account ID
     * @return list of characters
     */
    List<Character> findByAccountId(Long accountId);
    
    /**
     * Counts the number of characters for a given account.
     * @param accountId the account ID
     * @return count of characters
     */
    long countByAccountId(Long accountId);
}
