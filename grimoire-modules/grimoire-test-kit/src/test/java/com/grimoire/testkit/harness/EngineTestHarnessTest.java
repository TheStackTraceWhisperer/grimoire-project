package com.grimoire.testkit.harness;

import com.grimoire.application.core.ecs.GameSystem;
import com.grimoire.domain.core.component.Position;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link EngineTestHarness} verifying the harness lifecycle.
 */
class EngineTestHarnessTest extends EngineTestHarness {

    private final AtomicInteger systemTickCount = new AtomicInteger(0);

    @Override
    protected List<GameSystem> createSystems() {
        return List.of(deltaTime -> systemTickCount.incrementAndGet());
    }

    @Test
    void worldIsInitialised() {
        assertThat(world).isNotNull();
    }

    @Test
    void schedulerIsInitialised() {
        assertThat(scheduler).isNotNull();
    }

    @Test
    void gameEventPortIsAvailable() {
        assertThat(gameEventPort).isNotNull();
        assertThat(gameEventPort.totalEventCount()).isZero();
    }

    @Test
    void gameConfigIsAvailable() {
        assertThat(gameConfig).isNotNull();
        assertThat(gameConfig.attackRange()).isEqualTo(50.0);
    }

    @Test
    void tickInvokesRegisteredSystems() {
        systemTickCount.set(0);

        tick();

        assertThat(systemTickCount.get()).isEqualTo(1);
    }

    @Test
    void tickIncrementsWorldTickCounter() {
        long before = world.getCurrentTick();

        tick();

        assertThat(world.getCurrentTick()).isEqualTo(before + 1);
    }

    @Test
    void entityCreationAndComponentAddWork() {
        int entityId = world.createEntity();
        world.addComponent(entityId, new Position(10.0, 20.0));

        Position pos = world.getComponent(entityId, Position.class);
        assertThat(pos).isNotNull();
        assertThat(pos.x).isEqualTo(10.0);
        assertThat(pos.y).isEqualTo(20.0);
    }

    @Test
    void eachTestGetsCleanWorld() {
        // This test runs after others — world should be clean
        assertThat(world.getCurrentTick()).isZero();
    }
}
