package com.grimoire.server.network;

import com.grimoire.shared.codec.ForyDecoder;
import com.grimoire.shared.codec.ForyEncoder;
import io.micronaut.context.BeanContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.ssl.SslContext;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;

/**
 * Initializes the Netty channel pipeline with optional SSL support.
 */
@Singleton
@RequiredArgsConstructor
public class GameChannelInitializer extends ChannelInitializer<SocketChannel> {
    
    private final BeanContext beanContext;
    private SslContext sslContext;
    
    public void setSslContext(SslContext sslContext) {
        this.sslContext = sslContext;
    }
    
    @Override
    protected void initChannel(SocketChannel ch) {
        ChannelPipeline pipeline = ch.pipeline();
        
        // Add SSL handler if configured
        if (sslContext != null) {
            pipeline.addLast("ssl", sslContext.newHandler(ch.alloc()));
        }
        
        // Add codec handlers
        pipeline.addLast("encoder", new ForyEncoder());
        pipeline.addLast("decoder", new ForyDecoder());
        
        // Add game logic handler (create new instance per connection)
        pipeline.addLast("handler", beanContext.createBean(GameLogicHandler.class));
    }
}
