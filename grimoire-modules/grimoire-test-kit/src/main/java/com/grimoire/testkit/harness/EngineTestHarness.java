package com.grimoire.testkit.harness;

import com.grimoire.application.core.ecs.ComponentManager;
import com.grimoire.application.core.ecs.EcsWorld;
import com.grimoire.application.core.ecs.EntityManager;
import com.grimoire.application.core.ecs.GameSystem;
import com.grimoire.application.core.ecs.SystemScheduler;
import com.grimoire.application.core.port.GameConfig;
import com.grimoire.testkit.fake.FakeGameEventPort;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.junit.jupiter.api.BeforeEach;

import java.util.List;

/**
 * Abstract base class for tests that need a live ECS world with a system
 * scheduler.
 *
 * <p>
 * Inspired by the {@code october} project's {@code EngineTestHarness}, but
 * adapted for Grimoire's server-side ECS architecture (no OpenGL, no DI
 * container). Subclasses get a fully initialised {@link EcsWorld} and
 * {@link SystemScheduler} with overridable system lists and port fakes.
 * </p>
 *
 * <p>
 * Usage:
 * </p>
 *
 * <pre>
 * {
 *     &#64;code
 *     class MySystemTest extends EngineTestHarness {
 *         &#64;Override
 *         protected List<GameSystem> createSystems() {
 *             return List.of(new MySystem(world));
 *         }
 *
 *         @Test
 *         void myBehaviour() {
 *             // set up entities...
 *             tick();
 *             // assert results...
 *         }
 *     }
 * }
 * </pre>
 */
@SuppressWarnings("PMD.AbstractClassWithoutAbstractMethod")
@SuppressFBWarnings(value = "URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD", justification = "Protected fields are intended for subclass access in test code")
public abstract class EngineTestHarness {

    /** Default delta time for {@link #tick()} in seconds (50 ms = 20 TPS). */
    private static final float DEFAULT_DELTA = 0.05f;

    /** The ECS world — accessible in subclasses for entity/component setup. */
    protected EcsWorld world;

    /** The system scheduler that drives ticks. */
    protected SystemScheduler scheduler;

    /** The fake game event port — accessible for event assertions. */
    protected FakeGameEventPort gameEventPort;

    /** The game config — accessible for tuning parameters. */
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
     * Executes one tick with the given delta time.
     *
     * @param deltaTime
     *            time elapsed since last tick in seconds
     */
    protected void tick(float deltaTime) {
        scheduler.tick(deltaTime);
    }

    /**
     * Executes one tick with the default delta time (50 ms).
     */
    protected void tick() {
        tick(DEFAULT_DELTA);
    }
}
