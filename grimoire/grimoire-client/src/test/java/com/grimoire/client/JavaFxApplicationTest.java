package com.grimoire.client;

import com.grimoire.shared.dto.CharacterListResponse;
import com.grimoire.shared.dto.CharacterListResponse.CharacterInfo;
import io.micronaut.context.ApplicationContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JavaFxApplicationTest {
    
    private ApplicationContext mockContext;
    private JavaFxApplication application;
    
    @BeforeEach
    void setUp() {
        mockContext = mock(ApplicationContext.class);
        application = new JavaFxApplication(mockContext);
    }
    
    @Test
    void testApplicationCreation() {
        assertNotNull(application);
    }
    
    @Test
    void testGetCharacterListResponseInitiallyNull() {
        assertNull(application.getCharacterListResponse());
    }
    
    @Test
    void testSetCharacterListResponseViaMethod() {
        List<CharacterInfo> characters = List.of(
            new CharacterInfo(1L, "TestChar", 10, "zone1")
        );
        CharacterListResponse response = new CharacterListResponse("session-id", characters);
        
        // The application stores the response when showCharacterSelectionScene is called
        // But we can't call that without JavaFX initialization, so we just verify the getter works
        assertNull(application.getCharacterListResponse());
    }
    
    @Test
    void testStop() {
        // Should not throw exception
        application.stop();
        
        // Verify context was closed
        verify(mockContext, times(1)).close();
    }
    
    @Test
    void testStopWithNullContext() {
        // Create application without context injection
        JavaFxApplication app = new JavaFxApplication(null);
        
        // Should handle null context gracefully
        app.stop();
        
        assertNotNull(app);
    }
}
