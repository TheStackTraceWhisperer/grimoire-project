package com.grimoire.server.component;

import io.netty.channel.Channel;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

class PlayerConnectionTest {
    
    @Test
    void testPlayerConnectionCreation() {
        Channel mockChannel = Mockito.mock(Channel.class);
        PlayerConnection connection = new PlayerConnection(mockChannel);
        
        assertSame(mockChannel, connection.channel());
    }
    
    @Test
    void testPlayerConnectionIsComponent() {
        Channel mockChannel = Mockito.mock(Channel.class);
        PlayerConnection connection = new PlayerConnection(mockChannel);
        assertInstanceOf(Component.class, connection);
    }
}
