package com.grimoire.shared.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TokenLoginRequestTest {

    @Test
    void testTokenLoginRequestCreation() {
        TokenLoginRequest request = new TokenLoginRequest("test-access-token-12345");
        
        assertEquals("test-access-token-12345", request.accessToken());
    }

    @Test
    void testTokenLoginRequestIsSerializable() {
        TokenLoginRequest request = new TokenLoginRequest("token");
        assertInstanceOf(java.io.Serializable.class, request);
    }
}
