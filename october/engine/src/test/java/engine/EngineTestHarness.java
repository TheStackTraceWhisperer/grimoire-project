package engine;

import engine.services.state.ApplicationState;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Primary;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;

/**
 * An abstract base class that bootstraps a live, fully-functional game engine
 * within a JUnit 5 test environment. Tests requiring a live OpenGL context
 * should extend this class.
 */
@MicronautTest(packages = "engine")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class EngineTestHarness {

  @Inject
  protected Engine engine;

  /**
   * Initializes the engine once before any tests in the class run.
   * This creates the window and a valid OpenGL context.
   */
  @BeforeAll
  void startEngine() {
    engine.init();
  }

  /**
   * Shuts down the engine once after all tests in the class have finished.
   */
  @AfterAll
  void stopEngine() {
    engine.shutdown();
  }

  /**
   * Executes a single frame of the main game loop.
   * This is the primary way tests should interact with and update the engine state.
   */
  protected void tick() {
    engine.tick();
  }

  /**
   * A Micronaut Factory to provide test-specific beans that override
   * the production configuration.
   */
  @Slf4j
  @Factory
  static class TestConfiguration {

    /**
     * Overrides the production initial state with a no-op state for testing.
     * This prevents game logic from running automatically.
     */
    @Primary
    @Singleton
    @Named("initial")
    static class TestApplicationState implements ApplicationState {
      @Override
      public void onEnter() {
        log.info("Entering test harness application state.");
      }

      @Override
      public void onUpdate(float deltaTime) {
        // No-op for harness
      }

      @Override
      public void onExit() {
        // No-op for harness
      }
    }

    /**
     * Overrides the production game loop policy to prevent the engine from
     * entering an infinite loop, allowing tests to run and complete.
     */
    @Primary
    @Singleton
    public ApplicationLoopPolicy testLoopPolicy() {
      return ApplicationLoopPolicy.skip();
    }
  }
}
