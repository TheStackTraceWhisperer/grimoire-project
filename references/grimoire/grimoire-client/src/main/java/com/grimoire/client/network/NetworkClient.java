package com.grimoire.client.network;

import com.grimoire.shared.codec.ForyDecoder;
import com.grimoire.shared.codec.ForyEncoder;
import com.grimoire.shared.protocol.GamePacket;
import io.micronaut.context.BeanContext;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

/**
 * Network client for connecting to the game server.
 */
@Singleton
@Slf4j
public class NetworkClient {
    
    private final BeanContext beanContext;
    private EventLoopGroup group;
    private Channel channel;
    
    @Inject
    public NetworkClient(BeanContext beanContext) {
        this.beanContext = beanContext;
    }
    
    /**
     * Connects to the game server.
     */
    public void connect(String host, int port) throws Exception {
        group = new NioEventLoopGroup();
        
        try {
            // Create SSL context that trusts the server's self-signed certificate
            // WARNING: This is for development only. In production, use proper certificate validation.
            SslContext sslContext = SslContextBuilder
                    .forClient()
                    .trustManager(InsecureTrustManagerFactory.INSTANCE)
                    .build();
            
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ChannelPipeline pipeline = ch.pipeline();
                            // Add SSL handler first in the pipeline
                            pipeline.addLast("ssl", sslContext.newHandler(ch.alloc(), host, port));
                            pipeline.addLast("encoder", new ForyEncoder());
                            pipeline.addLast("decoder", new ForyDecoder());
                            pipeline.addLast("handler", beanContext.createBean(ClientLogicHandler.class));
                        }
                    });
            
            log.info("Connecting to server at {}:{} with SSL/TLS", host, port);
            channel = bootstrap.connect(host, port).sync().channel();
            log.info("Connected to server");
        } catch (Exception e) {
            log.error("Failed to connect", e);
            if (group != null) {
                group.shutdownGracefully();
            }
            throw e;
        }
    }
    
    /**
     * Sends a packet to the server.
     */
    public void send(GamePacket packet) {
        if (channel != null && channel.isActive()) {
            channel.writeAndFlush(packet);
        } else {
            log.warn("Cannot send packet, channel is not active");
        }
    }
    
    /**
     * Disconnects from the server.
     */
    public void disconnect() {
        if (channel != null) {
            channel.close();
        }
        if (group != null) {
            group.shutdownGracefully();
        }
    }
    
    /**
     * Checks if connected to the server.
     */
    public boolean isConnected() {
        return channel != null && channel.isActive();
    }
}
