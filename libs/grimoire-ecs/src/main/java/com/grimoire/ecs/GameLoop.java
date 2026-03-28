package com.grimoire.ecs;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

/**
 * Main game loop running at 20 ticks per second (50ms).
 * 
 * <p>This class provides the core game loop logic but does not include
 * scheduling annotations. Scheduling should be applied at the application
 * level to enable compile-time configuration.</p>
 */
@Singleton
@SuppressWarnings("PMD.CommentRequired")
public class GameLoop {
    
    private final SystemScheduler systemScheduler;
    private final GameCommandQueue commandQueue;

    @Inject
    @SuppressFBWarnings(
        value = "EI_EXPOSE_REP2",
        justification = "SystemScheduler and GameCommandQueue are DI-managed collaborators intentionally shared by reference."
    )
    public GameLoop(SystemScheduler systemScheduler, GameCommandQueue commandQueue) {
        this.systemScheduler = systemScheduler;
        this.commandQueue = commandQueue;
    }
    
    /**
     * Executes the game tick.
     * 
     * <p>First drains all pending network commands to ensure they are
     * processed on the main game thread, then executes all systems.</p>
     */
    public void tick() {
        // Process all pending network commands first
        commandQueue.drain();
        
        // Then execute all systems
        systemScheduler.tick(0.05f);
    }
}
