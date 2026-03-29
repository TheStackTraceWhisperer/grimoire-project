package engine;

import engine.services.state.ApplicationStateService;
import engine.services.time.SystemTimeService;
import engine.services.window.WindowService;
import jakarta.inject.Singleton;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Comparator;
import java.util.List;

@Slf4j
@Singleton
@RequiredArgsConstructor
public final class Engine implements Runnable {

  private enum State {
    NEW,
    INITIALIZED,
    SHUTDOWN
  }

  private final ApplicationLoopPolicy loopPolicy;
  private final ApplicationStateService applicationStateService;
  private final List<IService> services;
  @Getter
  private final WindowService windowService;
  private final SystemTimeService systemTimeService;

  private State state = State.NEW;
  private int frames = 0;

  public void init() {
    if (state != State.NEW) {
      log.warn("Engine has already been initialized.");
      return;
    }
    try {
      log.info("Initializing September Engine with DI container");

      services.sort(Comparator.comparingInt(IService::executionOrder));

      services.forEach(iService -> {
        log.debug("Starting service {}", iService.getClass().getSimpleName());
        iService.start();
      });

      log.info("September Engine initialized successfully");
      state = State.INITIALIZED;

    } catch (Exception e) {
      shutdown();
      throw new RuntimeException("Engine initialization failed", e);
    }
  }

  private void mainLoop() {
    while (shouldContinueLoop()) {
      tick();
    }
  }

  private boolean shouldContinueLoop() {
    boolean policyAllowsRunning = loopPolicy.continueRunning(frames, windowService.getHandle());
    boolean applicationStateIsNotEmpty = !applicationStateService.isEmpty();
    return policyAllowsRunning && applicationStateIsNotEmpty;
  }

  public void tick() {
    if (state != State.INITIALIZED) {
      throw new IllegalStateException("Cannot tick engine that is not initialized.");
    }
    windowService.pollEvents();
    services.forEach(IService::update);
    float dt = systemTimeService.getDeltaTimeSeconds();
    services.forEach(service -> service.update(dt));
    windowService.swapBuffers();
    frames++;
  }

  public void shutdown() {
    if (state == State.SHUTDOWN) {
      log.warn("Engine has already been shut down.");
      return;
    }
    log.info("Shutting down September Engine");

    services.sort(Comparator.comparingInt(IService::executionOrder).reversed());

    services.forEach(iService -> {
      log.debug("Stopping service {}", iService.getClass().getSimpleName());
      iService.stop();
    });

    state = State.SHUTDOWN;
  }

  @Override
  public void run() {
    try {
      init();
      mainLoop();
    } finally {
      shutdown();
    }
  }
}
