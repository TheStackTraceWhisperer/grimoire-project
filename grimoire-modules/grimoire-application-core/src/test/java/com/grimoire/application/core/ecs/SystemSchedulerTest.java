package com.grimoire.application.core.ecs;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class SystemSchedulerTest {

    private EcsWorld world;

    @BeforeEach
    void setUp() {
        world = new EcsWorld(new EntityManager(), new ComponentManager());
    }

    @Test
    void tickCallsAllSystemsInOrder() {
        GameSystem s1 = mock(GameSystem.class);
        GameSystem s2 = mock(GameSystem.class);
        GameSystem s3 = mock(GameSystem.class);

        var scheduler = new SystemScheduler(world, List.of(s1, s2, s3));
        scheduler.tick(0.05f);

        InOrder inOrder = Mockito.inOrder(s1, s2, s3);
        inOrder.verify(s1).tick(0.05f);
        inOrder.verify(s2).tick(0.05f);
        inOrder.verify(s3).tick(0.05f);
    }

    @Test
    void tickIncrementsTick() {
        var scheduler = new SystemScheduler(world, List.of());

        assertThat(world.getCurrentTick()).isZero();

        scheduler.tick(0.05f);
        assertThat(world.getCurrentTick()).isEqualTo(1);

        scheduler.tick(0.05f);
        assertThat(world.getCurrentTick()).isEqualTo(2);
    }

    @Test
    void emptySystemListOnlyIncrementsTick() {
        var scheduler = new SystemScheduler(world, List.of());

        scheduler.tick(0.05f);

        assertThat(world.getCurrentTick()).isEqualTo(1);
    }

    @Test
    void systemCountReflectsRegisteredSystems() {
        GameSystem s1 = mock(GameSystem.class);
        GameSystem s2 = mock(GameSystem.class);

        var scheduler = new SystemScheduler(world, List.of(s1, s2));

        assertThat(scheduler.systemCount()).isEqualTo(2);
    }

    @Test
    void constructorRejectsNullWorld() {
        assertThatThrownBy(() -> new SystemScheduler(null, List.of()))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("ecsWorld");
    }

    @Test
    void constructorRejectsNullSystemsList() {
        assertThatThrownBy(() -> new SystemScheduler(world, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("systems");
    }

    @Test
    void systemListIsDefensivelyCopied() {
        GameSystem s1 = mock(GameSystem.class);
        List<GameSystem> mutableList = new java.util.ArrayList<>(List.of(s1));

        var scheduler = new SystemScheduler(world, mutableList);
        mutableList.clear();

        assertThat(scheduler.systemCount()).isEqualTo(1);
    }

    @Test
    void multipleTicksCallSystemsEachTime() {
        GameSystem system = mock(GameSystem.class);
        var scheduler = new SystemScheduler(world, List.of(system));

        scheduler.tick(0.05f);
        scheduler.tick(0.05f);
        scheduler.tick(0.05f);

        verify(system, times(3)).tick(0.05f);
    }
}
