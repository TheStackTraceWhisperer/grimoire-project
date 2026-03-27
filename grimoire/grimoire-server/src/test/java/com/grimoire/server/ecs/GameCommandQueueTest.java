package com.grimoire.server.ecs;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class GameCommandQueueTest {

    @Test
    void testEnqueueAndDrain() {
        GameCommandQueue queue = new GameCommandQueue();
        AtomicInteger counter = new AtomicInteger(0);
        
        queue.enqueue(counter::incrementAndGet);
        queue.enqueue(counter::incrementAndGet);
        queue.enqueue(counter::incrementAndGet);
        
        assertEquals(3, queue.size());
        
        queue.drain();
        
        assertEquals(0, queue.size());
        assertEquals(3, counter.get());
    }
    
    @Test
    void testDrainEmptyQueue() {
        GameCommandQueue queue = new GameCommandQueue();
        
        // Should not throw
        queue.drain();
        
        assertEquals(0, queue.size());
    }
    
    @Test
    void testConcurrentEnqueue() throws InterruptedException {
        GameCommandQueue queue = new GameCommandQueue();
        AtomicInteger counter = new AtomicInteger(0);
        int numThreads = 10;
        int commandsPerThread = 100;
        
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(numThreads);
        
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        
        for (int i = 0; i < numThreads; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    for (int j = 0; j < commandsPerThread; j++) {
                        queue.enqueue(counter::incrementAndGet);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneLatch.countDown();
                }
            });
        }
        
        // Start all threads simultaneously
        startLatch.countDown();
        
        // Wait for all threads to finish
        assertTrue(doneLatch.await(5, TimeUnit.SECONDS));
        
        // Verify all commands were enqueued
        assertEquals(numThreads * commandsPerThread, queue.size());
        
        // Drain and verify all commands executed
        queue.drain();
        
        assertEquals(0, queue.size());
        assertEquals(numThreads * commandsPerThread, counter.get());
        
        executor.shutdown();
    }
    
    @Test
    void testCommandsExecuteInOrder() {
        GameCommandQueue queue = new GameCommandQueue();
        StringBuilder builder = new StringBuilder();
        
        queue.enqueue(() -> builder.append("A"));
        queue.enqueue(() -> builder.append("B"));
        queue.enqueue(() -> builder.append("C"));
        
        queue.drain();
        
        assertEquals("ABC", builder.toString());
    }
    
    @Test
    void testEnqueueDuringDrain() {
        GameCommandQueue queue = new GameCommandQueue();
        AtomicInteger counter = new AtomicInteger(0);
        
        // First command will enqueue another command during drain
        queue.enqueue(() -> {
            counter.incrementAndGet();
            queue.enqueue(counter::incrementAndGet);
        });
        
        // First drain should only execute commands that were enqueued before drain started
        // ConcurrentLinkedQueue.poll() will see the newly added command, so it will be executed
        queue.drain();
        
        // Both commands should have been executed since ConcurrentLinkedQueue sees new additions
        assertEquals(2, counter.get());
    }
}
