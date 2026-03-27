package com.grimoire.server.component;

import io.netty.channel.Channel;

/**
 * Player connection component holding the network channel.
 */
public record PlayerConnection(Channel channel) implements Component {
}
