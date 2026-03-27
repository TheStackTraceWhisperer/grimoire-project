package com.grimoire.client.network;

import io.micronaut.context.BeanContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import com.grimoire.shared.protocol.GamePacket;
import com.grimoire.shared.protocol.PacketType;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class NetworkClientTest {
    
    private BeanContext mockBeanContext;
    private NetworkClient networkClient;
    
    @BeforeEach
    void setUp() {
        mockBeanContext = Mockito.mock(BeanContext.class);
        ClientLogicHandler mockHandler = Mockito.mock(ClientLogicHandler.class);
        when(mockBeanContext.createBean(any())).thenReturn(mockHandler);
        
        networkClient = new NetworkClient(mockBeanContext);
    }
    
    @Test
    void testNetworkClientCreation() {
        assertNotNull(networkClient);
    }
    
    @Test
    void testIsConnectedReturnsFalseInitially() {
        assertFalse(networkClient.isConnected());
    }
    
    @Test
    void testDisconnectWithoutConnection() {
        // Should not throw exception when disconnecting without being connected
        networkClient.disconnect();
        assertFalse(networkClient.isConnected());
    }
    
    @Test
    void testSendPacketWhenNotConnected() {
        // Create a test packet
        GamePacket packet = new GamePacket(PacketType.C2S_MOVEMENT_INTENT, null);
        
        // Should not throw exception, but will log warning
        networkClient.send(packet);
        
        assertFalse(networkClient.isConnected());
    }
    
    @Test
    void testConnectFailsWithInvalidHost() {
        // Try to connect to invalid host
        assertThrows(Exception.class, () -> {
            networkClient.connect("invalid-host-that-does-not-exist", 9999);
        });
    }
    
    @Test
    void testDisconnectMultipleTimes() {
        // Should not throw exception when calling disconnect multiple times
        networkClient.disconnect();
        networkClient.disconnect();
        
        assertFalse(networkClient.isConnected());
    }
    
    @Test
    void testBeanContextInjection() {
        // Verify the bean context was injected correctly
        assertNotNull(networkClient);
        
        // The network client should be properly initialized with the mock bean context
        NetworkClient client = new NetworkClient(mockBeanContext);
        assertNotNull(client);
        assertFalse(client.isConnected());
    }
    
    @Test
    void testConnectWithNullHost() {
        // Try to connect with null host
        assertThrows(Exception.class, () -> {
            networkClient.connect(null, 9999);
        });
    }
    
    @Test
    void testConnectWithInvalidPort() {
        // Try to connect with invalid port
        assertThrows(Exception.class, () -> {
            networkClient.connect("localhost", -1);
        });
    }
    
    @Test
    void testSendMultiplePackets() {
        GamePacket packet1 = new GamePacket(PacketType.C2S_MOVEMENT_INTENT, null);
        GamePacket packet2 = new GamePacket(PacketType.C2S_CHAT_MESSAGE, null);
        
        // Should not throw exception when sending multiple packets while not connected
        networkClient.send(packet1);
        networkClient.send(packet2);
        
        assertFalse(networkClient.isConnected());
    }
    
    @Test
    void testIsConnectedAfterDisconnect() {
        // Initially not connected
        assertFalse(networkClient.isConnected());
        
        // Disconnect without connecting first
        networkClient.disconnect();
        
        // Still not connected
        assertFalse(networkClient.isConnected());
    }
}
