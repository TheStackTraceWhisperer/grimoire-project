package engine;

import engine.services.state.ApplicationStateService;
import engine.services.time.SystemTimeService;
import engine.services.window.WindowService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the {@link Engine} class, focusing on its lifecycle and state management.
 * This test is isolated from the Micronaut context and uses mocks for all dependencies.
 */
@ExtendWith(MockitoExtension.class)
class EngineTest {

  @Mock
  private ApplicationLoopPolicy loopPolicy;
  @Mock
  private ApplicationStateService applicationStateService;
  @Mock
  private WindowService windowService;
  @Mock
  private SystemTimeService systemTimeService;
  @Mock
  private IService serviceEarly; // e.g., executionOrder 1
  @Mock
  private IService serviceLate;  // e.g., executionOrder 10
  @Mock
  private IService serviceMiddle; // e.g., executionOrder 5

  private Engine engine;
  private List<IService> mutableServices;

  @BeforeEach
  void setUp() {
    // Configure service execution orders (leniently, as not all tests directly use these stubs)
    lenient().when(serviceEarly.executionOrder()).thenReturn(1);
    lenient().when(serviceMiddle.executionOrder()).thenReturn(5);
    lenient().when(serviceLate.executionOrder()).thenReturn(10);

    // The Engine requires a mutable list to sort, so we must provide an ArrayList.
    // Add them in a non-sorted order to ensure the engine's sorting logic is tested.
    mutableServices = new ArrayList<>(List.of(serviceLate, serviceEarly, serviceMiddle));
    engine = new Engine(loopPolicy, applicationStateService, mutableServices, windowService, systemTimeService);

    // Default mock behavior for loop policy and application state for most tests.
    // Marked as lenient to avoid UnnecessaryStubbingException if not used in every test.
    lenient().when(loopPolicy.continueRunning(anyInt(), anyLong())).thenReturn(true);
    lenient().when(applicationStateService.isEmpty()).thenReturn(false);
  }

  @Test
  void init_whenCalledMultipleTimes_onlyInitializesServicesOnce() {
    // Act
    engine.init();
    engine.init(); // Second call should be ignored

    // Assert
    // Verify that the start method on each service was called exactly once.
    verify(serviceEarly, times(1)).start();
    verify(serviceMiddle, times(1)).start();
    verify(serviceLate, times(1)).start();
  }

  @Test
  void init_startsServicesInExecutionOrder() {
    // Act
    engine.init();

    // Assert
    InOrder inOrder = inOrder(serviceEarly, serviceMiddle, serviceLate);
    inOrder.verify(serviceEarly).start();
    inOrder.verify(serviceMiddle).start();
    inOrder.verify(serviceLate).start();
  }

  @Test
  void shutdown_whenCalledMultipleTimes_onlyShutsDownServicesOnce() {
    // Arrange
    engine.init(); // Engine must be initialized to be shut down

    // Act
    engine.shutdown();
    engine.shutdown(); // Second call should be ignored

    // Assert
    // Verify that the stop method on each service was called exactly once.
    verify(serviceEarly, times(1)).stop();
    verify(serviceMiddle, times(1)).stop();
    verify(serviceLate, times(1)).stop();
  }

  @Test
  void shutdown_stopsServicesInReverseExecutionOrder() {
    // Arrange
    engine.init(); // Initialize first to set up the state

    // Act
    engine.shutdown();

    // Assert
    InOrder inOrder = inOrder(serviceLate, serviceMiddle, serviceEarly);
    inOrder.verify(serviceLate).stop();
    inOrder.verify(serviceMiddle).stop();
    inOrder.verify(serviceEarly).stop();
  }

  @Test
  void tick_whenEngineNotInitialized_throwsIllegalStateException() {
    // Assert
    // Verify that calling tick() on a new engine throws the correct exception.
    assertThrows(IllegalStateException.class, () -> engine.tick());
  }

  @Test
  void tick_whenEngineInitialized_performsExpectedActions() {
    // Arrange
    engine.init();
    float expectedDeltaTime = 0.016f; // Example delta time
    when(systemTimeService.getDeltaTimeSeconds()).thenReturn(expectedDeltaTime);

    // Act
    engine.tick();

    // Assert
    verify(windowService).pollEvents();
    verify(serviceEarly).update();
    verify(serviceMiddle).update();
    verify(serviceLate).update();
    verify(serviceEarly).update(expectedDeltaTime);
    verify(serviceMiddle).update(expectedDeltaTime);
    verify(serviceLate).update(expectedDeltaTime);
    verify(windowService).swapBuffers();
    // We can't easily verify the 'frames' counter directly without a getter,
    // but its effect on loopPolicy.continueRunning is tested in run_executesMainLoop.
  }

  @Test
  void run_whenLoopDoesNotRun_stillCallsInitAndShutdown() {
    // Arrange
    // Configure the loop policy to immediately exit the main loop.
    when(loopPolicy.continueRunning(anyInt(), anyLong())).thenReturn(false);

    // Act
    engine.run();

    // Assert
    // Verify that init() and shutdown() were both called once as part of the run() lifecycle.
    verify(serviceEarly, times(1)).start();
    verify(serviceEarly, times(1)).stop();
    verify(serviceMiddle, times(1)).start();
    verify(serviceMiddle, times(1)).stop();
    verify(serviceLate, times(1)).start();
    verify(serviceLate, times(1)).stop();
  }

  @Test
  void run_executesMainLoopUntilConditionFalse() {
    // Arrange
    // Configure loop to run 3 times, then stop
    when(loopPolicy.continueRunning(eq(0), anyLong())).thenReturn(true);
    when(loopPolicy.continueRunning(eq(1), anyLong())).thenReturn(true);
    when(loopPolicy.continueRunning(eq(2), anyLong())).thenReturn(true);
    when(loopPolicy.continueRunning(eq(3), anyLong())).thenReturn(false); // Stop after 3 ticks

    // Act
    engine.run();

    // Assert
    // Verify init and shutdown are called once
    verify(serviceEarly, times(1)).start();
    verify(serviceEarly, times(1)).stop();

    // Verify tick() was called 3 times (for frames 0, 1, 2)
    verify(windowService, times(3)).pollEvents();
    verify(windowService, times(3)).swapBuffers();
    verify(serviceEarly, times(3)).update();
    verify(serviceMiddle, times(3)).update();
    verify(serviceLate, times(3)).update();
  }

  @Test
  void run_shutsDownIfInitFails() {
    // Arrange
    doThrow(new RuntimeException("Service failed to start")).when(serviceEarly).start();

    // Act & Assert
    RuntimeException thrown = assertThrows(RuntimeException.class, () -> engine.run());
    // Verify that the exception from init is rethrown
    assert(thrown.getMessage().contains("Engine initialization failed"));

    // Verify that shutdown was called on services that might have started.
    // In this case, serviceEarly failed to start, but shutdown() is still called in finally.
    verify(serviceEarly, times(1)).start(); // Attempted to start
    verify(serviceEarly, times(1)).stop(); // stop() is called in finally block
    verify(serviceMiddle, never()).start(); // Never started
    verify(serviceLate, never()).start(); // Never started

    // If serviceMiddle had started before serviceEarly failed, it would also be stopped.
    // For this specific test, we only expect serviceEarly to be affected.
  }
}
