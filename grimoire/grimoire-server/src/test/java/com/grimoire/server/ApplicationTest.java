package com.grimoire.server;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ApplicationTest {
    
    @Test
    void testApplicationClassExists() {
        // Simple test to ensure the Application class is properly defined
        assertNotNull(Application.class);
    }
    
    @Test
    void testMainMethodExists() throws NoSuchMethodException {
        // Verify that main method exists with correct signature
        var mainMethod = Application.class.getDeclaredMethod("main", String[].class);
        assertNotNull(mainMethod);
        assertTrue(java.lang.reflect.Modifier.isStatic(mainMethod.getModifiers()));
        assertTrue(java.lang.reflect.Modifier.isPublic(mainMethod.getModifiers()));
    }
}
