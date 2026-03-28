package com.grimoire.network;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link BootstrapFactory}.
 */
class BootstrapFactoryTest {

    @Test
    void createEventLoopGroup_rejectsNonPositiveThreads() {
        BootstrapFactory factory = new BootstrapFactory();

        assertThatThrownBy(() -> factory.createEventLoopGroup(0, "boss"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("threads must be positive");
    }

    @Test
    void createEventLoopGroup_rejectsNullNamePrefix() {
        BootstrapFactory factory = new BootstrapFactory();

        assertThatThrownBy(() -> factory.createEventLoopGroup(1, null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("namePrefix cannot be null");
    }

    @Test
    void createEventLoopGroup_returnsWorkingGroup() {
        BootstrapFactory factory = new BootstrapFactory();

        EventLoopGroup group = factory.createEventLoopGroup(1, "test");
        try {
            assertThat(group).isNotNull();
            assertThat(group.isShutdown()).isFalse();
        } finally {
            group.shutdownGracefully().syncUninterruptibly();
        }
    }

    @Test
    void getServerSocketChannelClass_matchesPlatformAvailability() {
        BootstrapFactory factory = new BootstrapFactory();

        Class<? extends ServerSocketChannel> channelClass = factory.getServerSocketChannelClass();
        if (Epoll.isAvailable()) {
            assertThat(channelClass).isEqualTo(EpollServerSocketChannel.class);
        } else {
            assertThat(channelClass).isEqualTo(NioServerSocketChannel.class);
        }
    }
}

