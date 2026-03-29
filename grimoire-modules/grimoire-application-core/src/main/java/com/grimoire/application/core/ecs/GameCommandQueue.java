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

    /** Thread-safe queue for pending game commands. */
    private final Queue<Runnable> commands = new ConcurrentLinkedQueue<>();

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
     * Returns the number of pending commands.
     *
     * @return the queue size
     */
    public int size() {
        return commands.size();
    }
}
