package com.netbullet.net;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The core TCP Game Server. Manages the Netty lifecycle and binds to the
 * network port.
 *
 * <p>
 * <strong>Thread Safety:</strong> This class uses {@link ReentrantLock} for
 * state management to ensure compatibility with Java virtual threads. The
 * {@code start()}, {@code stop()}, and {@code getPort()} methods are
 * thread-safe. However, it is recommended that lifecycle methods (start/stop)
 * be called from a single coordinating thread or with external synchronization
 * to ensure predictable behavior.
 *
 * <p>
 * <strong>Resource Management:</strong> This class implements
 * {@link AutoCloseable} and ensures proper cleanup of Netty resources in all
 * cases, including startup failures.
 */
public final class GameServer implements AutoCloseable {

    /** Logger for lifecycle and error events. */
    private static final Logger LOG = LoggerFactory.getLogger(GameServer.class);
    /** Maximum wait for graceful shutdown in {@link #close()}. */
    private static final int CLOSE_TIMEOUT_SECONDS = 30;
    /** Shared PMD suppression name for required Netty lifecycle thread usage. */
    private static final String PMD_DO_NOT_USE_THREADS = "PMD.DoNotUseThreads";

    /** Factory for transport and event loop resources. */
    private final BootstrapFactory bootstrapFactory;
    /** Guards lifecycle state transitions across caller threads. */
    private final ReentrantLock stateLock = new ReentrantLock();
    /** Netty boss event loop group that accepts incoming connections. */
    @SuppressWarnings(PMD_DO_NOT_USE_THREADS)
    private EventLoopGroup bossGroup;
    /** Netty worker event loop group that processes accepted connections. */
    @SuppressWarnings(PMD_DO_NOT_USE_THREADS)
    private EventLoopGroup workerGroup;
    /** Channel reference written by async bind callback and read by callers. */
    @SuppressWarnings("PMD.AvoidUsingVolatile")
    private volatile Channel serverChannel;

    /**
     * Constructs a new GameServer.
     *
     * @param bootstrapFactory
     *            factory for creating transport objects
     * @throws IllegalArgumentException
     *             if bootstrapFactory is null
     */
    public GameServer(BootstrapFactory bootstrapFactory) {
        if (bootstrapFactory == null) {
            throw new IllegalArgumentException("bootstrapFactory cannot be null");
        }
        this.bootstrapFactory = bootstrapFactory;
    }

    /**
     * Starts the server on the specified port.
     *
     * <p>
     * The returned future completes successfully once the underlying Netty server
     * channel has been bound. If binding fails for any reason (for example, the
     * port is already in use, the port requires elevated privileges such as ports
     * 80/443/22, or the process lacks sufficient permissions), the future is
     * completed exceptionally with the cause reported by Netty and the server
     * remains stopped. EventLoopGroups are automatically cleaned up on failure.
     *
     * <p>
     * This method is not idempotent and is intended to be called at most once per
     * {@code GameServer} instance. Concurrent or repeated invocations are not
     * supported and may result in {@link IllegalStateException}; callers should
     * create a new {@code GameServer} instance if a restarted server is required.
     *
     * @param port
     *            the TCP port to bind to (0-65535, where 0 means ephemeral). Note
     *            that ports below 1024 typically require elevated privileges on
     *            Unix-like systems.
     * @return a future that completes when the server is bound, or completes
     *         exceptionally if the bind operation fails
     * @throws IllegalArgumentException
     *             if port is outside valid range (0-65535)
     * @throws IllegalStateException
     *             if server is already started or shutting down
     */
    @SuppressWarnings({ "PMD.CognitiveComplexity", "PMD.AvoidCatchingGenericException", PMD_DO_NOT_USE_THREADS })
    public CompletableFuture<Void> start(int port) {
        if (port < 0 || port > 65_535) {
            IllegalArgumentException ex = new IllegalArgumentException(
                    "Port must be between 0 and 65_535 inclusive, but was " + port);
            LOG.error("Invalid port for GameServer.start: {}", port);
            CompletableFuture<Void> failedFuture = new CompletableFuture<>();
            failedFuture.completeExceptionally(ex);
            return failedFuture;
        }

        stateLock.lock();
        try {
            if (bossGroup != null || workerGroup != null) {
                boolean bossActive = bossGroup != null && !bossGroup.isTerminated();
                boolean workerActive = workerGroup != null && !workerGroup.isTerminated();
                if (bossActive || workerActive) {
                    throw new IllegalStateException(
                            "GameServer is already started or is shutting down; call stop() before starting again.");
                }
            }

            this.bossGroup = bootstrapFactory.createEventLoopGroup(1, "boss");
            this.workerGroup = bootstrapFactory.createEventLoopGroup(Runtime.getRuntime().availableProcessors(),
                    "worker");
        } finally {
            stateLock.unlock();
        }

        ServerBootstrap b = new ServerBootstrap();
        b.group(bossGroup, workerGroup)
                .channel(bootstrapFactory.getServerSocketChannelClass())
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        // Pipeline is empty for Phase 1
                    }
                });

        CompletableFuture<Void> startupFuture = new CompletableFuture<>();
        try {
            b.bind(port).addListener((ChannelFuture future) -> {
                if (future.isSuccess()) {
                    this.serverChannel = future.channel();
                    InetSocketAddress addr = (InetSocketAddress) serverChannel.localAddress();
                    if (LOG.isInfoEnabled()) {
                        LOG.info("GameServer started on port: {}", addr.getPort());
                    }
                    startupFuture.complete(null);
                } else {
                    Throwable cause = future.cause();
                    if (port > 0 && port < 1024) {
                        LOG.error("Failed to bind privileged port; elevated permissions may be required.");
                    } else {
                        LOG.error("Failed to bind requested port; port may be in use or inaccessible.");
                    }
                    LOG.debug("Bind failure details", cause);
                    shutdownEventLoopGroupsOnStartFailure();
                    startupFuture.completeExceptionally(cause);
                }
            });
        } catch (RuntimeException e) {
            LOG.error("Exception while binding requested port.");
            LOG.debug("Bind exception details", e);
            shutdownEventLoopGroupsOnStartFailure();
            startupFuture.completeExceptionally(e);
        }

        return startupFuture;
    }

    /**
     * Shuts down event loop groups when server startup fails. This is only used on
     * start() failure paths to avoid leaking Netty threads.
     */
    @SuppressWarnings(PMD_DO_NOT_USE_THREADS)
    private void shutdownEventLoopGroupsOnStartFailure() {
        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
        }
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
        }
    }

    /**
     * Returns the actual TCP port the server is currently listening on.
     *
     * @return the TCP port number if the server is running and bound, or {@code -1}
     *         if the server has not been started yet or has already been stopped
     */
    public int getPort() {
        if (serverChannel != null && serverChannel.isOpen()) {
            return ((InetSocketAddress) serverChannel.localAddress()).getPort();
        }
        return -1;
    }

    /**
     * Stops the server and releases all resources.
     *
     * <p>
     * This method gracefully shuts down both the boss and worker EventLoopGroups
     * and waits for both to complete before returning. The returned future
     * completes only when all resources have been released.
     *
     * @return a future that completes when shutdown is finished
     */
    @SuppressWarnings({ "PMD.CognitiveComplexity", "PMD.DoNotUseThreads" })
    public CompletableFuture<Void> stop() {
        LOG.info("Stopping GameServer...");
        CompletableFuture<Void> shutdownFuture = new CompletableFuture<>();

        if (bossGroup != null || workerGroup != null) {
            CompletableFuture<Void> bossFuture = new CompletableFuture<>();
            CompletableFuture<Void> workerFuture = new CompletableFuture<>();

            if (bossGroup != null) {
                bossGroup.shutdownGracefully().addListener(future -> {
                    if (future.isSuccess()) {
                        bossFuture.complete(null);
                    } else {
                        bossFuture.completeExceptionally(future.cause());
                    }
                });
            } else {
                bossFuture.complete(null);
            }

            if (workerGroup != null) {
                workerGroup.shutdownGracefully().addListener(future -> {
                    if (future.isSuccess()) {
                        workerFuture.complete(null);
                    } else {
                        workerFuture.completeExceptionally(future.cause());
                    }
                });
            } else {
                workerFuture.complete(null);
            }

            CompletableFuture.allOf(bossFuture, workerFuture).whenComplete((ignored, t) -> {
                if (t != null) {
                    LOG.error("Error during shutdown", t);
                    shutdownFuture.completeExceptionally(t);
                } else {
                    LOG.info("GameServer stopped.");
                    shutdownFuture.complete(null);
                }
            });
        } else {
            shutdownFuture.complete(null);
        }
        return shutdownFuture;
    }

    /**
     * Closes the server and releases all resources with a bounded timeout.
     *
     * <p>
     * This method calls {@code stop()} and waits up to 30 seconds for graceful
     * shutdown to complete. If shutdown does not complete within the timeout, the
     * method returns anyway to prevent indefinite blocking. This ensures
     * {@code close()} has bounded execution time as required for production use.
     *
     * <p>
     * This method is called automatically when using try-with-resources.
     *
     * @throws IllegalStateException
     *             if shutdown fails or is interrupted
     */
    @Override
    @SuppressWarnings(PMD_DO_NOT_USE_THREADS)
    public void close() {
        try {
            stop().get(CLOSE_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOG.error("Interrupted while closing GameServer");
            throw new IllegalStateException("Interrupted while closing GameServer", e);
        } catch (java.util.concurrent.ExecutionException | java.util.concurrent.TimeoutException e) {
            LOG.error("Error or timeout during close() - shutdown may not be complete", e);
            throw new IllegalStateException("Failed to close GameServer cleanly", e);
        }
    }
}
