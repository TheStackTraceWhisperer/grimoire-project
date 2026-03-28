package com.grimoire.network;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.ServerSocket;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

/**
 * Unit tests for {@link NetworkServer}.
 */
class NetworkServerTest {

    private NetworkServer startedServer;

    @AfterEach
    void cleanup() {
        if (startedServer != null) {
            startedServer.shutdown();
            startedServer = null;
        }
    }

    @Test
    void getPort_returnsNegativeOne_whenNotStarted() {
        BootstrapFactory factory = new BootstrapFactory();
        ChannelInitializer<SocketChannel> initializer = new ChannelInitializer<>() {
            @Override
            protected void initChannel(SocketChannel ch) {
                // no-op for coverage test server
            }
        };
        NetworkServer server = new NetworkServer(factory, initializer);

        assertThat(server.getPort()).isEqualTo(-1);
        assertThatNoException().isThrownBy(server::shutdown);
    }

    @Test
    void start_bindsToEphemeralPort_andShutdownClosesChannel() {
        NetworkServer server = new NetworkServer(new BootstrapFactory(), noopInitializer());
        setIntField(server, "port", 0);
        setIntField(server, "bossThreads", 1);
        setIntField(server, "workerThreads", 0);

        server.start();
        startedServer = server;

        int boundPort = waitForBoundPort(server);
        assertThat(boundPort).isPositive();

        server.shutdown();
        startedServer = null;
        assertThat(server.getPort()).isEqualTo(-1);
    }

    @Test
    void start_whenPortAlreadyInUse_keepsServerStopped() throws IOException {
        int occupiedPort;
        try (ServerSocket socket = new ServerSocket(0)) {
            occupiedPort = socket.getLocalPort();

            NetworkServer server = new NetworkServer(new BootstrapFactory(), noopInitializer());
            setIntField(server, "port", occupiedPort);
            setIntField(server, "bossThreads", 1);
            setIntField(server, "workerThreads", 1);

            server.start();

            // Bind happens asynchronously; wait briefly for listener execution.
            sleepMillis(400);
            assertThat(server.getPort()).isEqualTo(-1);
            assertThatNoException().isThrownBy(server::shutdown);
        }
    }

    private static ChannelInitializer<SocketChannel> noopInitializer() {
        return new ChannelInitializer<>() {
            @Override
            protected void initChannel(SocketChannel ch) {
                // no-op for tests
            }
        };
    }

    private static int waitForBoundPort(NetworkServer server) {
        long timeoutNanos = Duration.ofSeconds(3).toNanos();
        long deadline = System.nanoTime() + timeoutNanos;
        int port;
        do {
            port = server.getPort();
            if (port > 0) {
                return port;
            }
            sleepMillis(25);
        } while (System.nanoTime() < deadline);
        return port;
    }

    private static void setIntField(NetworkServer target, String fieldName, int value) {
        try {
            Field field = NetworkServer.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.setInt(target, value);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to set field: " + fieldName, e);
        }
    }

    private static void sleepMillis(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting for async bind", e);
        }
    }
}

