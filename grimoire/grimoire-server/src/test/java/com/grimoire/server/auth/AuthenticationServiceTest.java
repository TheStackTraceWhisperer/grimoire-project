package com.grimoire.server.auth;

import com.grimoire.data.Account;
import com.grimoire.data.AccountRepository;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.micronaut.test.support.TestPropertyProvider;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for AuthenticationService using Testcontainers.
 * Disabled by default - requires Docker runtime.
 * Run with: mvn test -Dtest=AuthenticationServiceTest
 */
@Disabled("Requires Docker runtime")
@MicronautTest
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AuthenticationServiceTest implements TestPropertyProvider {
    
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("grimoire_test")
            .withUsername("test")
            .withPassword("test");
    
    static {
        postgres.start();
    }
    
    @Inject
    AuthenticationService authService;
    
    @Inject
    AccountRepository accountRepository;
    
    @Override
    public Map<String, String> getProperties() {
        return Map.of(
                "datasources.default.url", postgres.getJdbcUrl(),
                "datasources.default.username", postgres.getUsername(),
                "datasources.default.password", postgres.getPassword(),
                "datasources.default.driver-class-name", "org.postgresql.Driver"
        );
    }
    
    @Test
    void testCreateOAuthAccount() {
        Optional<Account> account = authService.createOAuthAccount("oauth_user");
        
        assertTrue(account.isPresent());
        assertEquals("oauth_user", account.get().getUsername());
    }
    
    @Test
    void testCreateOAuthAccountReturnExisting() {
        authService.createOAuthAccount("existing_user");
        Optional<Account> duplicate = authService.createOAuthAccount("existing_user");
        
        assertTrue(duplicate.isPresent());
        assertEquals("existing_user", duplicate.get().getUsername());
    }
    
    @Test
    void testFindByUsername() {
        authService.createOAuthAccount("findme");
        Optional<Account> account = authService.findByUsername("findme");
        
        assertTrue(account.isPresent());
        assertEquals("findme", account.get().getUsername());
    }
    
    @Test
    void testFindByUsernameNotFound() {
        Optional<Account> account = authService.findByUsername("nonexistent");
        
        assertFalse(account.isPresent());
    }
}
