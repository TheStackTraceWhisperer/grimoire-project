package com.grimoire.server.network;

import io.micronaut.context.BeanContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.ssl.SslContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GameChannelInitializerTest {
    
    private BeanContext mockBeanContext;
    private GameChannelInitializer initializer;
    
    @BeforeEach
    void setUp() {
        mockBeanContext = Mockito.mock(BeanContext.class);
        initializer = new GameChannelInitializer(mockBeanContext);
    }
    
    @Test
    void testSetSslContext() {
        SslContext mockSslContext = Mockito.mock(SslContext.class);
        
        initializer.setSslContext(mockSslContext);
        
        // Should not throw exception
        assertNotNull(initializer);
    }
    
    @Test
    void testSetNullSslContext() {
        initializer.setSslContext(null);
        
        // Should not throw exception
        assertNotNull(initializer);
    }
    
    @Test
    void testInitChannelWithoutSsl() {
        SocketChannel mockChannel = Mockito.mock(SocketChannel.class);
        ChannelPipeline mockPipeline = Mockito.mock(ChannelPipeline.class);
        GameLogicHandler mockHandler = Mockito.mock(GameLogicHandler.class);
        
        when(mockChannel.pipeline()).thenReturn(mockPipeline);
        when(mockPipeline.addLast(anyString(), any())).thenReturn(mockPipeline);
        when(mockBeanContext.createBean(GameLogicHandler.class)).thenReturn(mockHandler);
        
        initializer.initChannel(mockChannel);
        
        // Verify pipeline setup (encoder, decoder, handler)
        verify(mockPipeline, atLeast(3)).addLast(anyString(), any());
        verify(mockBeanContext).createBean(GameLogicHandler.class);
    }
}
