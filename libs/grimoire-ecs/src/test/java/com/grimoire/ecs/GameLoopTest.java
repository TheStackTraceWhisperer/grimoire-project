package com.grimoire.ecs;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GameLoopTest {
    
    @Test
    void testGameLoopTick() {
        SystemScheduler mockScheduler = Mockito.mock(SystemScheduler.class);
        GameCommandQueue commandQueue = new GameCommandQueue();
        GameLoop gameLoop = new GameLoop(mockScheduler, commandQueue);
        
        gameLoop.tick();
        
        // Verify the scheduler was called with correct delta time
        verify(mockScheduler, times(1)).tick(0.05f);
    }
    
    @Test
    void testGameLoopMultipleTicks() {
        SystemScheduler mockScheduler = Mockito.mock(SystemScheduler.class);
        GameCommandQueue commandQueue = new GameCommandQueue();
        GameLoop gameLoop = new GameLoop(mockScheduler, commandQueue);
        
        gameLoop.tick();
        gameLoop.tick();
        gameLoop.tick();
        
        // Verify the scheduler was called three times
        verify(mockScheduler, times(3)).tick(0.05f);
    }
    
    @Test
    void testGameLoopDrainsCommandQueue() {
        SystemScheduler mockScheduler = Mockito.mock(SystemScheduler.class);
        GameCommandQueue commandQueue = new GameCommandQueue();
        GameLoop gameLoop = new GameLoop(mockScheduler, commandQueue);
        
        // Enqueue some commands
        final int[] counter = {0};
        commandQueue.enqueue(() -> counter[0]++);
        commandQueue.enqueue(() -> counter[0]++);
        
        // Execute tick
        gameLoop.tick();
        
        // Verify commands were executed
        assertEquals(2, counter[0]);
    }
}
