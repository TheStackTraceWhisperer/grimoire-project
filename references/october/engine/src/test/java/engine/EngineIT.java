package engine;

import engine.services.time.SystemTimeService;
import engine.services.window.WindowService;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test to verify that the EngineTestHarness can successfully
 * bootstrap the engine and its core services.
 */
class EngineIT extends EngineTestHarness {

  @Inject
  private WindowService windowService;

  @Inject
  private SystemTimeService systemTimeService;

  @Test
  void engineAndWindowServiceShouldBeInitialized() {
    // The 'engine' field is inherited from the harness and should be injected.
    assertThat(engine).isNotNull();

    // The WindowService should be injected and have created a valid window handle.
    assertThat(windowService).isNotNull();
    assertThat(windowService.getHandle())
      .withFailMessage("Window handle should have been created by the EngineTestHarness")
      .isNotZero();
  }

  @Test
  void systemTimeService_updatesTimeOnTick() throws InterruptedException {
    // Arrange
    // Initial values should be 0 before any ticks
    assertThat(systemTimeService.getDeltaTimeSeconds()).isZero();
    assertThat(systemTimeService.getTotalTimeSeconds()).isZero();

    // Act
    // Tick the engine a few times to allow time to pass
    tick();
    Thread.sleep(10); // Simulate some time passing between ticks
    tick();
    Thread.sleep(10); // Simulate some time passing between ticks
    tick();

    // Assert
    // After ticking, delta and total time should be greater than zero
    assertThat(systemTimeService.getDeltaTimeSeconds())
      .withFailMessage("Delta time should be greater than 0 after engine ticks")
      .isGreaterThan(0.0f);
    assertThat(systemTimeService.getTotalTimeSeconds())
      .withFailMessage("Total time should be greater than 0 after engine ticks")
      .isGreaterThan(0.0);

    // Verify that total time is greater than delta time (assuming multiple ticks)
    assertThat(systemTimeService.getTotalTimeSeconds()).isGreaterThanOrEqualTo(systemTimeService.getDeltaTimeSeconds());
  }
}
