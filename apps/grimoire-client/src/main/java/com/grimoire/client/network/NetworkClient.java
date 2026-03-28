package com.grimoire.client.network;

import com.grimoire.shared.codec.ForyDecoder;
import com.grimoire.shared.codec.ForyEncoder;
import com.grimoire.shared.protocol.GamePacket;
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
import lombok.extern.slf4j.Slf4j;

import java.util.function.Consumer;

/**
 * Network client for connecting to the game server.
 * Ported from grimoire-client for LWJGL-based client.
 */
@Slf4j
public class NetworkClient {
    
    private EventLoopGroup group;
    private Channel channel;
    private Consumer<GamePacket> packetHandler;
    private Runnable disconnectHandler;
    
    /**
     * Sets the packet handler for received packets.
     */
    public void setPacketHandler(Consumer<GamePacket> handler) {
        this.packetHandler = handler;
    }
    
    /**
     * Sets the disconnect handler.
     */
    public void setDisconnectHandler(Runnable handler) {
        this.disconnectHandler = handler;
    }
    
    /**
     * Connects to the game server.
     *
     * @param host Server hostname
     * @param port Server port
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
            
            NetworkClient client = this;
            
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
                            pipeline.addLast("handler", new ClientLogicHandler(client));
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
    
    /**
     * Called by the handler when a packet is received.
     */
    void onPacketReceived(GamePacket packet) {
        if (packetHandler != null) {
            packetHandler.accept(packet);
        }
    }
    
    /**
     * Called by the handler when disconnected.
     */
    void onDisconnected() {
        if (disconnectHandler != null) {
            disconnectHandler.run();
        }
    }
}
