package com.grimoire.server.network;

import io.micronaut.context.annotation.Value;
import io.micronaut.context.event.ApplicationEventListener;
import io.micronaut.runtime.event.ApplicationStartupEvent;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.net.ssl.KeyManagerFactory;
import java.io.InputStream;
import java.security.KeyStore;

/**
 * Netty game server bootstrap with SSL/TLS support.
 */
@Singleton
@RequiredArgsConstructor
@Slf4j
public class GameServer implements ApplicationEventListener<ApplicationStartupEvent> {
    
    @Value("${game.server.port:8888}")
    private int port;
    
    @Value("${game.server.ssl.enabled:false}")
    private boolean sslEnabled;
    
    @Value("${game.server.ssl.keystore:classpath:certs/server-keystore.p12}")
    private String keystorePath;
    
    @Value("${game.server.ssl.keystorePassword:grimoire}")
    private String keystorePassword;
    
    @Value("${game.server.ssl.keystoreType:PKCS12}")
    private String keystoreType;
    
    private final GameChannelInitializer channelInitializer;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private SslContext sslContext;
    
    @Override
    public void onApplicationEvent(ApplicationStartupEvent event) {
        new Thread(this::start, "game-server").start();
    }
    
    private void initSslContext() throws Exception {
        if (!sslEnabled) {
            return;
        }
        
        String keystoreResource = keystorePath.replace("classpath:", "");
        try (InputStream keystoreStream = getClass().getClassLoader().getResourceAsStream(keystoreResource)) {
            if (keystoreStream == null) {
                throw new IllegalStateException("Keystore not found: " + keystorePath);
            }
            
            KeyStore keyStore = KeyStore.getInstance(keystoreType);
            keyStore.load(keystoreStream, keystorePassword.toCharArray());
            
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(keyStore, keystorePassword.toCharArray());
            
            sslContext = SslContextBuilder
                    .forServer(kmf)
                    .build();
            
            log.info("SSL/TLS enabled for game server");
        }
    }
    
    public void start() {
        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup();
        
        try {
            initSslContext();
            
            channelInitializer.setSslContext(sslContext);
            
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(channelInitializer)
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);
            
            String sslStatus = sslEnabled ? " (SSL/TLS enabled)" : "";
            log.info("Starting game server on port {}{}", port, sslStatus);
            ChannelFuture future = bootstrap.bind(port).sync();
            log.info("Game server started successfully");
            
            future.channel().closeFuture().sync();
        } catch (Exception e) {
            log.error("Game server error", e);
        } finally {
            shutdown();
        }
    }
    
    public void shutdown() {
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
        }
        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
        }
    }
}
