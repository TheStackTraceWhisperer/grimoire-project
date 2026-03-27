package com.grimoire.server.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.micronaut.security.token.jwt.validator.JwtValidator;
import java.util.Optional;
import java.util.Map;
import java.util.HashMap;

/**
 * Tests for TokenValidationService.
 */
class TokenValidationServiceTest {
    
    private JwtValidator jwtValidator;
    private TokenValidationService tokenValidationService;
    
    @BeforeEach
    void setUp() {
        jwtValidator = mock(JwtValidator.class);
        tokenValidationService = new TokenValidationService(jwtValidator);
    }
    
    @Test
    void testValidateTokenAndGetUsername_ValidToken() {
        // Arrange
        String token = "valid.jwt.token";
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", "testuser");
        when(jwtValidator.validate(eq(token), any())).thenReturn(Optional.of(claims));
        
        // Act
        Optional<String> result = tokenValidationService.validateTokenAndGetUsername(token);
        
        // Assert
        assertTrue(result.isPresent());
        assertEquals("testuser", result.get());
    }
    
    @Test
    void testValidateTokenAndGetUsername_TokenWithBearerPrefix() {
        // Arrange
        String token = "Bearer valid.jwt.token";
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", "testuser");
        when(jwtValidator.validate(eq("valid.jwt.token"), any())).thenReturn(Optional.of(claims));
        
        // Act
        Optional<String> result = tokenValidationService.validateTokenAndGetUsername(token);
        
        // Assert
        assertTrue(result.isPresent());
        assertEquals("testuser", result.get());
    }
    
    @Test
    void testValidateTokenAndGetUsername_InvalidToken() {
        // Arrange
        String token = "invalid.jwt.token";
        when(jwtValidator.validate(eq(token), any())).thenReturn(Optional.empty());
        
        // Act
        Optional<String> result = tokenValidationService.validateTokenAndGetUsername(token);
        
        // Assert
        assertFalse(result.isPresent());
    }
    
    @Test
    void testValidateTokenAndGetUsername_TokenWithoutSubClaim() {
        // Arrange
        String token = "valid.jwt.token";
        Map<String, Object> claims = new HashMap<>();
        // Missing "sub" claim
        when(jwtValidator.validate(eq(token), any())).thenReturn(Optional.of(claims));
        
        // Act
        Optional<String> result = tokenValidationService.validateTokenAndGetUsername(token);
        
        // Assert
        assertFalse(result.isPresent());
    }
    
    @Test
    void testValidateTokenAndGetUsername_ValidationThrowsException() {
        // Arrange
        String token = "valid.jwt.token";
        when(jwtValidator.validate(eq(token), any())).thenThrow(new RuntimeException("Validation error"));
        
        // Act
        Optional<String> result = tokenValidationService.validateTokenAndGetUsername(token);
        
        // Assert
        assertFalse(result.isPresent());
    }
}
