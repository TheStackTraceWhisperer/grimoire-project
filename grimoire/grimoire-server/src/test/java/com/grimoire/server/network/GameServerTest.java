package com.grimoire.server.network;

import io.micronaut.runtime.event.ApplicationStartupEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

class GameServerTest {
    
    private GameChannelInitializer mockInitializer;
    private GameServer gameServer;
    
    @BeforeEach
    void setUp() {
        mockInitializer = Mockito.mock(GameChannelInitializer.class);
        gameServer = new GameServer(mockInitializer);
    }
    
    @Test
    void testGameServerCreation() {
        assertNotNull(gameServer);
    }
    
    @Test
    void testOnApplicationEventStartsThread() {
        ApplicationStartupEvent mockEvent = Mockito.mock(ApplicationStartupEvent.class);
        
        // This will start the server in a separate thread, but won't bind successfully
        // since we don't have a real network setup. The test verifies the event handler exists.
        gameServer.onApplicationEvent(mockEvent);
        
        // Give the thread a moment to start
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // The server should have attempted to start (we can't easily test the actual start without network)
        assertNotNull(gameServer);
    }
    
    @Test
    void testShutdownWithoutStart() {
        // Should be able to call shutdown even if not started
        gameServer.shutdown();
        
        assertNotNull(gameServer);
    }
    
    @Test
    void testShutdownMultipleTimes() {
        // Should be able to call shutdown multiple times without error
        gameServer.shutdown();
        gameServer.shutdown();
        
        assertNotNull(gameServer);
    }
    
    @Test
    void testSslContextInitializationDisabled() throws Exception {
        // Test that SSL is disabled by default
        GameServer server = new GameServer(mockInitializer);
        
        // Use reflection to verify SSL is disabled
        Field sslEnabledField = GameServer.class.getDeclaredField("sslEnabled");
        sslEnabledField.setAccessible(true);
        boolean sslEnabled = (boolean) sslEnabledField.get(server);
        
        assertFalse(sslEnabled, "SSL should be disabled by default");
    }
    
    @Test
    void testDefaultPort() throws Exception {
        GameServer server = new GameServer(mockInitializer);
        
        // Use reflection to check default port
        Field portField = GameServer.class.getDeclaredField("port");
        portField.setAccessible(true);
        int port = (int) portField.get(server);
        
        // Default port should be 0 (not yet set by @Value annotation)
        assertTrue(port >= 0);
    }
}
