package com.grimoire.infra.network.registry;

import io.netty.channel.Channel;
import io.netty.channel.ChannelId;
import jakarta.inject.Singleton;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Maintains the physical mapping between Netty {@link Channel} objects and
 * logical Player/Session UUIDs.
 *
 * <p>
 * This is required for the ECS to send outbound packets to specific users. The
 * registry is thread-safe for concurrent access from Netty I/O threads.
 * </p>
 */
@Singleton
public class NetworkSessionRegistry {

    /** Maps internal Netty ChannelId to the actual TCP Channel. */
    private final ConcurrentMap<ChannelId, Channel> activeChannels = new ConcurrentHashMap<>();

    /** Maps Authenticated Session UUIDs to their current Netty ChannelId. */
    private final ConcurrentMap<UUID, ChannelId> authenticatedSessions = new ConcurrentHashMap<>();

    /**
     * Registers a newly connected channel.
     *
     * @param channel
     *            the Netty channel
     */
    public void registerChannel(Channel channel) {
        activeChannels.put(channel.id(), channel);
    }

    /**
     * Removes a disconnected channel and cleans up any associated session.
     *
     * @param channel
     *            the Netty channel
     */
    public void removeChannel(Channel channel) {
        activeChannels.remove(channel.id());
        // Reverse lookup to clean up authenticated sessions if needed
        authenticatedSessions.values().removeIf(id -> id.equals(channel.id()));
    }

    /**
     * Associates an authenticated session UUID with a Netty channel.
     *
     * @param sessionId
     *            the session UUID
     * @param channelId
     *            the Netty channel ID
     */
    public void mapSessionToChannel(UUID sessionId, ChannelId channelId) {
        authenticatedSessions.put(sessionId, channelId);
    }

    /**
     * Looks up the channel for a given authenticated session.
     *
     * @param sessionId
     *            the session UUID
     * @return the channel, or {@code null} if not found
     */
    public Channel getChannelBySession(UUID sessionId) {
        ChannelId cid = authenticatedSessions.get(sessionId);
        if (cid != null) {
            return activeChannels.get(cid);
        }
        return null;
    }
}
