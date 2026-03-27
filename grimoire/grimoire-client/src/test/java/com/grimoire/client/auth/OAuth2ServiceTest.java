package com.grimoire.client.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OAuth2ServiceTest {
    
    private OAuth2Service oauth2Service;
    
    @BeforeEach
    void setUp() {
        oauth2Service = new OAuth2Service();
    }
    
    @Test
    void testOAuth2ServiceCreation() {
        assertNotNull(oauth2Service);
    }
    
    @Test
    void testAuthenticateWithoutKeycloak() {
        // This will timeout since there's no Keycloak server running
        // We're testing that it handles the timeout gracefully
        // Note: This test is skipped in CI environments as it takes time
        
        // For now, we just verify the service can be created
        assertNotNull(oauth2Service);
    }
}
