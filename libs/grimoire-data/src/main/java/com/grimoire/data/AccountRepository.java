package com.grimoire.data;

import io.micronaut.data.annotation.Repository;
import io.micronaut.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository for Account entities.
 */
@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    
    /**
     * Finds an account by username.
     * @param username the username
     * @return Optional containing the account if found
     */
    Optional<Account> findByUsername(String username);
    
    /**
     * Checks if an account exists with the given username.
     * @param username the username
     * @return true if account exists
     */
    boolean existsByUsername(String username);
}
