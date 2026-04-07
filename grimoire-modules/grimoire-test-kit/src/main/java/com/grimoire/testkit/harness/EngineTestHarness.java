package com.grimoire.testkit.harness;

import com.grimoire.application.core.ecs.*;
import com.grimoire.application.core.port.GameConfig;
import com.grimoire.testkit.fake.FakeGameEventPort;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.junit.jupiter.api.BeforeEach;

import java.util.List;

/**
 * Abstract base class for tests that need a live ECS world with a system
 * scheduler.
 */
@SuppressWarnings("PMD.AbstractClassWithoutAbstractMethod")
@SuppressFBWarnings(value = "URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD", justification = "Protected fields are intended for subclass access in test code")
public abstract class EngineTestHarness {

    /**
     * The ECS world — accessible in subclasses for entity/component setup.
     */
    protected EcsWorld world;

    /**
     * The system scheduler that drives ticks.
     */
    protected SystemScheduler scheduler;

    /**
     * The fake game event port — accessible for event assertions.
     */
    protected FakeGameEventPort gameEventPort;

    /**
     * The game config — accessible for tuning parameters.
     */
    protected GameConfig gameConfig;

    /**
     * Initialises the ECS world, creates systems, and wires the scheduler.
     *
     * <p>
     * Runs before each test method to ensure a clean state.
     * </p>
     */
    @BeforeEach
    void setUpEngine() {
        EntityManager entityManager = new EntityManager();
        ComponentManager componentManager = new ComponentManager();
        world = new EcsWorld(entityManager, componentManager);
        gameEventPort = createGameEventPort();
        gameConfig = createGameConfig();
        scheduler = new SystemScheduler(world, createSystems());
    }

    /**
     * Returns the list of game systems to register with the scheduler.
     *
     * <p>
     * Override this to provide the systems under test. The default returns an empty
     * list (useful for pure ECS-world tests).
     * </p>
     *
     * @return ordered list of systems
     */
    protected List<GameSystem> createSystems() {
        return List.of();
    }

    /**
     * Creates the {@link GameEventPort} fake used during tests.
     *
     * <p>
     * Override to provide a custom implementation. The default returns a
     * {@link FakeGameEventPort}.
     * </p>
     *
     * @return the game event port
     */
    protected FakeGameEventPort createGameEventPort() {
        return new FakeGameEventPort();
    }

    /**
     * Creates the {@link GameConfig} used during tests.
     *
     * <p>
     * Override to provide custom tuning. The default returns a config with all
     * default values.
     * </p>
     *
     * @return the game config
     */
    protected GameConfig createGameConfig() {
        return new GameConfig() {
        };
    }

    /**
     * Executes one tick of the scheduler.
     */
    protected void tick() {
        scheduler.tick();
    }
}
