package com.grimoire.ecs;

import jakarta.inject.Singleton;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Thread-safe command queue for processing network commands on the game loop thread.
 * 
 * <p>Commands from Netty IO threads should be enqueued here rather than modifying
 * the ECS world directly. The GameLoop drains this queue at the start of each tick,
 * ensuring all ECS modifications happen on a single thread.</p>
 */
@Singleton
@SuppressWarnings("PMD.CommentRequired")
public class GameCommandQueue {
    
    private final Queue<Runnable> commands = new ConcurrentLinkedQueue<>();
    
    /**
     * Enqueues a command to be executed on the game loop thread.
     * This method is thread-safe and can be called from any thread.
     * 
     * @param command the command to execute
     */
    public void enqueue(Runnable command) {
        commands.add(command);
    }
    
    /**
     * Drains all pending commands and executes them.
     * This method should only be called from the game loop thread.
     */
    public void drain() {
        while (!commands.isEmpty()) {
            Runnable command = commands.poll();
            if (command == null) {
                continue;
            }
            command.run();
        }
    }
    
    /**
     * Returns the number of pending commands.
     * Useful for monitoring and debugging.
     * 
     * @return the number of pending commands
     */
    public int size() {
        return commands.size();
    }
}
