package com.grimoire.server.auth;

import com.grimoire.data.Account;
import com.grimoire.data.AccountRepository;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

/**
 * Service for managing OAuth2-authenticated accounts.
 * All authentication is handled by Keycloak/OAuth2.
 */
@Singleton
@RequiredArgsConstructor
public class AuthenticationService {
    
    private final AccountRepository accountRepository;
    
    /**
     * Finds an account by username.
     * @param username the username
     * @return Optional containing the account if found
     */
    public Optional<Account> findByUsername(String username) {
        return accountRepository.findByUsername(username);
    }
    
    /**
     * Creates a new account for OAuth2 authenticated user.
     * @param username the username from OAuth2 token
     * @return Optional containing the created account if successful
     */
    public Optional<Account> createOAuthAccount(String username) {
        if (accountRepository.existsByUsername(username)) {
            return accountRepository.findByUsername(username);
        }
        
        Account account = new Account(username);
        return Optional.of(accountRepository.save(account));
    }
}
