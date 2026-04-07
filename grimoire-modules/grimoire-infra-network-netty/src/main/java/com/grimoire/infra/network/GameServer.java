package com.grimoire.infra.network;

import com.grimoire.infra.network.factory.BootstrapFactory;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.util.concurrent.DefaultThreadFactory;
import jakarta.annotation.PreDestroy;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Netty TCP game server entry point.
 *
 * <p>
 * Micronaut manages this singleton. It boots the {@link ServerBootstrap}, binds
 * to the configured port, and holds the boss/worker thread pools. Graceful
 * shutdown is handled via {@link PreDestroy}.
 * </p>
 */
@Singleton
public class GameServer {

    private static final Logger LOG = LoggerFactory.getLogger(GameServer.class);

    private final BootstrapFactory bootstrapFactory;
    private final GameChannelInitializer channelInitializer;

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private ChannelFuture serverChannelFuture;

    /**
     * Creates a game server.
     *
     * @param bootstrapFactory
     *            factory for OS-optimized transport
     * @param channelInitializer
     *            pipeline initializer for new connections
     */
    @Inject
    public GameServer(BootstrapFactory bootstrapFactory,
            GameChannelInitializer channelInitializer) {
        this.bootstrapFactory = bootstrapFactory;
        this.channelInitializer = channelInitializer;
    }

    /**
     * Starts the Netty server and binds to the given port.
     *
     * @param port
     *            the TCP port to bind
     */
    public void start(int port) {
        if (LOG.isInfoEnabled()) {
            LOG.info("Starting Netty Game Server on port {}...", port);
            LOG.info("Epoll Available: {}", bootstrapFactory.isEpollAvailable());
        }

        bossGroup = bootstrapFactory.createBossGroup(1,
                new DefaultThreadFactory("netty-boss"));
        workerGroup = bootstrapFactory.createWorkerGroup(0,
                new DefaultThreadFactory("netty-worker")); // 0 = 2x CPU cores

        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(bootstrapFactory.getServerChannelClass())
                    .childHandler(channelInitializer)
                    .option(ChannelOption.SO_BACKLOG, 256)
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            // Bind and start to accept incoming connections.
            serverChannelFuture = b.bind(port).sync();
            LOG.info("Game Server successfully bound to port {}", port);

        } catch (InterruptedException e) {
            LOG.error("Game Server interrupted during boot", e);
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Gracefully shuts down the server and releases all thread pools.
     */
    @PreDestroy
    public void stop() {
        LOG.info("Shutting down Netty Game Server...");
        if (serverChannelFuture != null) {
            serverChannelFuture.channel().close();
        }
        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
        }
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
        }
        LOG.info("Netty Game Server shutdown complete.");
    }
}
