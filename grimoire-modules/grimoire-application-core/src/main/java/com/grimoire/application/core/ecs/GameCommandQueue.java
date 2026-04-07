package com.grimoire.application.core.ecs;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Thread-safe command queue for cross-thread communication with the game loop.
 *
 * <p>
 * Commands are enqueued from any thread (e.g., Netty IO threads) and drained on
 * the game loop thread during each tick. This is the only approved cross-thread
 * bridge in the ECS architecture.
 * </p>
 *
 * <p>
 * Register as a singleton at the assembly layer.
 * </p>
 */
public class GameCommandQueue {

    /**
     * Thread-safe queue for pending game commands.
     */
    private final Queue<Runnable> commands = new ConcurrentLinkedQueue<>();

    /**
     * Thread-safe queue for inbound network packets awaiting processing.
     */
    private final Queue<InboundPacket> inboundPackets = new ConcurrentLinkedQueue<>();

    /**
     * An inbound network packet with its source channel identifier.
     *
     * @param channelId
     *            short text identifier of the originating Netty channel
     * @param packet
     *            the decoded packet object
     */
    public record InboundPacket(String channelId, Object packet) {
    }

    /**
     * Enqueues a command to be executed on the next game tick.
     *
     * @param command
     *            the command to enqueue
     */
    public void enqueue(Runnable command) {
        commands.add(command);
    }

    /**
     * Enqueues an inbound network packet for processing on the next game tick.
     *
     * <p>
     * Called from Netty IO threads. The packet is stored with the originating
     * channel identifier so the game loop can attribute it to a session.
     * </p>
     *
     * @param channelId
     *            short text identifier of the originating Netty channel
     * @param packet
     *            the decoded packet object
     */
    public void enqueueInboundPacket(String channelId, Object packet) {
        inboundPackets.add(new InboundPacket(channelId, packet));
    }

    /**
     * Drains and executes all pending commands.
     *
     * <p>
     * Must be called from the game loop thread only.
     * </p>
     */
    @SuppressWarnings("PMD.AssignmentInOperand")
    public void drainAll() {
        Runnable command;
        while ((command = commands.poll()) != null) {
            command.run();
        }
    }

    /**
     * Drains all pending inbound packets and passes each to the given consumer.
     *
     * <p>
     * Must be called from the game loop thread only.
     * </p>
     *
     * @param consumer
     *            handler for each inbound packet
     */
    @SuppressWarnings("PMD.AssignmentInOperand")
    public void drainInboundPackets(java.util.function.Consumer<InboundPacket> consumer) {
        InboundPacket pkt;
        while ((pkt = inboundPackets.poll()) != null) {
            consumer.accept(pkt);
        }
    }

    /**
     * Returns the number of pending commands.
     *
     * @return the queue size
     */
    public int size() {
        return commands.size();
    }

    /**
     * Returns the number of pending inbound packets.
     *
     * @return the inbound packet queue size
     */
    public int inboundPacketCount() {
        return inboundPackets.size();
    }
}
