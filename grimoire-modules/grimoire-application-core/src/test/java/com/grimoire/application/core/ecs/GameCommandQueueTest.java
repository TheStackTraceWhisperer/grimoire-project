package com.grimoire.application.core.ecs;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GameCommandQueueTest {

    private GameCommandQueue queue;

    @BeforeEach
    void setUp() {
        queue = new GameCommandQueue();
    }

    @Test
    void initialSizeIsZero() {
        assertThat(queue.size()).isZero();
    }

    @Test
    void enqueuedCommandIncrementsSize() {
        queue.enqueue(() -> {
        });

        assertThat(queue.size()).isEqualTo(1);
    }

    @Test
    void drainAllExecutesAllCommands() {
        List<String> log = new ArrayList<>();
        queue.enqueue(() -> log.add("a"));
        queue.enqueue(() -> log.add("b"));
        queue.enqueue(() -> log.add("c"));

        queue.drainAll();

        assertThat(log).containsExactly("a", "b", "c");
    }

    @Test
    void drainAllEmptiesQueue() {
        queue.enqueue(() -> {
        });
        queue.enqueue(() -> {
        });

        queue.drainAll();

        assertThat(queue.size()).isZero();
    }

    @Test
    void drainAllOnEmptyQueueDoesNothing() {
        queue.drainAll();

        assertThat(queue.size()).isZero();
    }

    @Test
    void commandsExecuteInFifoOrder() {
        List<Integer> order = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            int value = i;
            queue.enqueue(() -> order.add(value));
        }

        queue.drainAll();

        assertThat(order).containsExactly(0, 1, 2, 3, 4);
    }

    @Test
    void multipleEnqueueAndDrainCycles() {
        List<String> log = new ArrayList<>();

        queue.enqueue(() -> log.add("first"));
        queue.drainAll();
        assertThat(log).containsExactly("first");

        queue.enqueue(() -> log.add("second"));
        queue.drainAll();
        assertThat(log).containsExactly("first", "second");
    }
}
