package com.grimoire.network;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.micronaut.context.annotation.Value;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import jakarta.annotation.PreDestroy;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

/**
 * Micronaut-managed Netty TCP server that drives the game transport layer.
 *
 * <p>To start the server, call {@link #start()} explicitly (typically from the application
 * context). The server will shut down gracefully on {@link PreDestroy} (i.e., when the
 * application context closes). Channel pipeline setup is fully delegated to the injected
 * {@link ChannelInitializer} bean, keeping this class free of protocol-specific logic.</p>
 *
 * <p><strong>Configuration keys:</strong>
 * <ul>
 *   <li>{@code game.server.port} (default: {@code 8888}) — TCP port to bind.</li>
 *   <li>{@code game.server.boss-threads} (default: {@code 1}) — Netty boss group threads.</li>
 *   <li>{@code game.server.worker-threads} (default: {@code 0}) — Netty worker group threads;
 *       {@code 0} means {@code 2 × availableProcessors}.</li>
 * </ul>
 *
 * <p><strong>Thread Safety:</strong> The {@code start()} method and {@code @PreDestroy}
 * contract must not be called concurrently; start/stop should be coordinated by the caller.</p>
 */
@Singleton
@Slf4j
@SuppressWarnings("PMD.CommentRequired")
public class NetworkServer {

    private static final int SHUTDOWN_TIMEOUT_SECONDS = 30;

    @Value("${game.server.port:8888}")
    private int port;

    @Value("${game.server.boss-threads:1}")
    private int bossThreads;

    @Value("${game.server.worker-threads:0}")
    private int workerThreads;

    private final BootstrapFactory bootstrapFactory;
    private final ChannelInitializer<SocketChannel> channelInitializer;

    @SuppressWarnings("PMD.DoNotUseThreads")
    private EventLoopGroup bossGroup;
    @SuppressWarnings("PMD.DoNotUseThreads")
    private EventLoopGroup workerGroup;
    @SuppressWarnings("PMD.AvoidUsingVolatile")
    private volatile Channel serverChannel;

    @Inject
    @SuppressFBWarnings(
        value = "EI_EXPOSE_REP2",
        justification = "BootstrapFactory and ChannelInitializer are DI-managed collaborators intentionally shared by reference."
    )
    public NetworkServer(BootstrapFactory bootstrapFactory, ChannelInitializer<SocketChannel> channelInitializer) {
        this.bootstrapFactory = bootstrapFactory;
        this.channelInitializer = channelInitializer;
    }

    /**
     * Starts the network server by binding to the configured port.
     * Must be called explicitly from the application context (typically during startup).
     */
    public void start() {
        int effectiveWorkers = workerThreads > 0
                ? workerThreads
                : Runtime.getRuntime().availableProcessors() * 2;

        bossGroup   = bootstrapFactory.createEventLoopGroup(bossThreads, "netty-boss");
        workerGroup = bootstrapFactory.createEventLoopGroup(effectiveWorkers, "netty-worker");

        ServerBootstrap bootstrap = new ServerBootstrap()
                .group(bossGroup, workerGroup)
                .channel(bootstrapFactory.getServerSocketChannelClass())
                .childHandler(channelInitializer);

        ChannelFuture bindFuture = bootstrap.bind(port);
        bindFuture.addListener((ChannelFuture future) -> {
            if (future.isSuccess()) {
                serverChannel = future.channel();
                int boundPort = ((InetSocketAddress) serverChannel.localAddress()).getPort();
                log.info("NetworkServer bound and listening on port {}", boundPort);
            } else {
                if (log.isErrorEnabled()) {
                    log.error("NetworkServer failed to bind", future.cause());
                }
                shutdownGroups();
            }
        });
    }

    /**
     * Returns the actual port the server is currently bound to, or {@code -1} when stopped.
     *
     * @return bound TCP port, or {@code -1}
     */
    public int getPort() {
        if (serverChannel != null && serverChannel.isOpen()) {
            return ((InetSocketAddress) serverChannel.localAddress()).getPort();
        }
        return -1;
    }

    /**
     * Gracefully shuts down both EventLoopGroups and closes the server channel.
     */
    @PreDestroy
    public void shutdown() {
        log.info("Shutting down NetworkServer...");
        if (serverChannel != null) {
            serverChannel.close().awaitUninterruptibly(SHUTDOWN_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        }
        shutdownGroups();
        log.info("NetworkServer shut down.");
    }

    @SuppressWarnings("PMD.DoNotUseThreads")
    private void shutdownGroups() {
        if (bossGroup != null) {
            bossGroup.shutdownGracefully().awaitUninterruptibly(SHUTDOWN_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        }
        if (workerGroup != null) {
            workerGroup.shutdownGracefully().awaitUninterruptibly(SHUTDOWN_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        }
    }
}

