package com.grimoire.server.auth;

import com.grimoire.data.Account;
import com.grimoire.data.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthenticationServiceUnitTest {
    
    private AccountRepository mockRepository;
    private AuthenticationService authService;
    
    @BeforeEach
    void setUp() {
        mockRepository = Mockito.mock(AccountRepository.class);
        authService = new AuthenticationService(mockRepository);
    }
    
    @Test
    void testFindByUsername() {
        Account mockAccount = Mockito.mock(Account.class);
        when(mockAccount.getUsername()).thenReturn("testuser");
        when(mockRepository.findByUsername("testuser")).thenReturn(Optional.of(mockAccount));
        
        Optional<Account> result = authService.findByUsername("testuser");
        
        assertTrue(result.isPresent());
        assertEquals("testuser", result.get().getUsername());
        verify(mockRepository).findByUsername("testuser");
    }
    
    @Test
    void testFindByUsernameNotFound() {
        when(mockRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());
        
        Optional<Account> result = authService.findByUsername("nonexistent");
        
        assertFalse(result.isPresent());
        verify(mockRepository).findByUsername("nonexistent");
    }
    
    @Test
    void testCreateOAuthAccountNew() {
        Account mockAccount = Mockito.mock(Account.class);
        when(mockRepository.existsByUsername("newuser")).thenReturn(false);
        when(mockRepository.save(any(Account.class))).thenReturn(mockAccount);
        
        Optional<Account> result = authService.createOAuthAccount("newuser");
        
        assertTrue(result.isPresent());
        verify(mockRepository).existsByUsername("newuser");
        verify(mockRepository).save(any(Account.class));
    }
    
    @Test
    void testCreateOAuthAccountExisting() {
        Account mockAccount = Mockito.mock(Account.class);
        when(mockRepository.existsByUsername("existinguser")).thenReturn(true);
        when(mockRepository.findByUsername("existinguser")).thenReturn(Optional.of(mockAccount));
        
        Optional<Account> result = authService.createOAuthAccount("existinguser");
        
        assertTrue(result.isPresent());
        verify(mockRepository).existsByUsername("existinguser");
        verify(mockRepository).findByUsername("existinguser");
        verify(mockRepository, never()).save(any());
    }
}
