package com.grimoire.server.security;

import io.micronaut.security.token.jwt.signature.jwks.JwksSignature;
import io.micronaut.security.token.jwt.validator.JwtValidator;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Optional;

/**
 * Service for validating OAuth2 JWT tokens from the authorization server.
 */
@Singleton
@RequiredArgsConstructor
@Slf4j
public class TokenValidationService {
    
    private final JwtValidator jwtValidator;
    
    /**
     * Validates a JWT token and extracts the username.
     * @param token the JWT access token
     * @return Optional containing the username if token is valid
     */
    public Optional<String> validateTokenAndGetUsername(String token) {
        try {
            // Remove "Bearer " prefix if present
            String actualToken = token.startsWith("Bearer ") ? token.substring(7) : token;
            
            // Parse and validate token
            Optional<?> validationResult = jwtValidator.validate(actualToken, null);
            
            if (validationResult.isPresent()) {
                Object claims = validationResult.get();
                if (claims instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> claimsMap = (Map<String, Object>) claims;
                    String username = (String) claimsMap.get("sub");
                    if (username != null) {
                        log.info("Token validated successfully for user: {}", username);
                        return Optional.of(username);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Token validation failed", e);
        }
        return Optional.empty();
    }
}
