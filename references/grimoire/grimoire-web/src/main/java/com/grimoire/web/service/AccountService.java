package com.grimoire.web.service;

import com.grimoire.data.Account;
import com.grimoire.data.AccountRepository;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

/**
 * Service for OAuth2-authenticated account management.
 * All authentication is handled by Keycloak/OAuth2.
 */
@Singleton
@RequiredArgsConstructor
public class AccountService {
    
    private final AccountRepository accountRepository;
    
    /**
     * Creates or retrieves an account for OAuth2 authenticated user.
     * @param username the username from OAuth2
     * @return Optional containing the account
     */
    public Optional<Account> getOrCreateOAuthAccount(String username) {
        Optional<Account> accountOpt = accountRepository.findByUsername(username);
        if (accountOpt.isPresent()) {
            return accountOpt;
        }
        
        Account account = new Account(username);
        return Optional.of(accountRepository.save(account));
    }
    
    /**
     * Finds an account by username.
     * @param username the username
     * @return Optional containing the account if found
     */
    public Optional<Account> findByUsername(String username) {
        return accountRepository.findByUsername(username);
    }
    
    /**
     * Finds an account by ID.
     * @param id the account ID
     * @return Optional containing the account if found
     */
    public Optional<Account> findById(Long id) {
        return accountRepository.findById(id);
    }
}
