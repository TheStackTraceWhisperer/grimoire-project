package com.grimoire.infra.network.factory;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.ServerChannel;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import jakarta.inject.Singleton;

import java.util.concurrent.ThreadFactory;

/**
 * OS-level optimization factory for Netty transport.
 *
 * <p>
 * Automatically detects Linux environments to utilize edge-triggered Epoll for
 * maximum socket throughput, falling back to NIO for Windows/macOS.
 * </p>
 */
@Singleton
public class BootstrapFactory {

    /**
     * Checks if the Epoll transport is available on this platform.
     *
     * @return {@code true} on Linux with native Epoll support
     */
    public boolean isEpollAvailable() {
        return Epoll.isAvailable();
    }

    /**
     * Creates the boss event loop group for accepting connections.
     *
     * @param threads
     *            the number of threads (typically 1)
     * @param threadFactory
     *            the thread factory for naming
     * @return an Epoll or NIO event loop group
     */
    public EventLoopGroup createBossGroup(int threads, ThreadFactory threadFactory) {
        return isEpollAvailable()
                ? new EpollEventLoopGroup(threads, threadFactory)
                : new NioEventLoopGroup(threads, threadFactory);
    }

    /**
     * Creates the worker event loop group for handling I/O.
     *
     * @param threads
     *            the number of threads (0 = 2× CPU cores)
     * @param threadFactory
     *            the thread factory for naming
     * @return an Epoll or NIO event loop group
     */
    public EventLoopGroup createWorkerGroup(int threads, ThreadFactory threadFactory) {
        return isEpollAvailable()
                ? new EpollEventLoopGroup(threads, threadFactory)
                : new NioEventLoopGroup(threads, threadFactory);
    }

    /**
     * Returns the appropriate server channel class for this platform.
     *
     * @return Epoll or NIO server socket channel class
     */
    public Class<? extends ServerChannel> getServerChannelClass() {
        return isEpollAvailable()
                ? EpollServerSocketChannel.class
                : NioServerSocketChannel.class;
    }
}
