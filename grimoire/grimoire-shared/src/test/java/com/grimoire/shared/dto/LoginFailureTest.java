package com.grimoire.shared.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LoginFailureTest {

    @Test
    void testLoginFailureCreation() {
        LoginFailure failure = new LoginFailure("Invalid credentials");
        
        assertEquals("Invalid credentials", failure.reason());
    }

    @Test
    void testLoginFailureIsSerializable() {
        LoginFailure failure = new LoginFailure("Test reason");
        assertInstanceOf(java.io.Serializable.class, failure);
    }
}
