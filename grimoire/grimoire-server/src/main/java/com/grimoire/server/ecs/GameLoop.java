package com.grimoire.server.ecs;

import io.micronaut.scheduling.annotation.Scheduled;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;

/**
 * Main game loop running at 20 ticks per second (50ms).
 */
@Singleton
@RequiredArgsConstructor
public class GameLoop {
    
    private final SystemScheduler systemScheduler;
    private final GameCommandQueue commandQueue;
    
    /**
     * Executes the game tick at a fixed rate of 50ms (20 TPS).
     * 
     * <p>First drains all pending network commands to ensure they are
     * processed on the main game thread, then executes all systems.</p>
     */
    @Scheduled(fixedRate = "50ms")
    public void tick() {
        // Process all pending network commands first
        commandQueue.drain();
        
        // Then execute all systems
        systemScheduler.tick(0.05f);
    }
}
