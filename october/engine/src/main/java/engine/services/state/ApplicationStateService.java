package engine.services.state;

import engine.IService;
import engine.services.world.WorldService;
import io.micronaut.context.ApplicationContext;
import jakarta.inject.Named;
import jakarta.inject.Provider;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Stack;
import java.util.function.Supplier;

/**
 * A stack-based Finite State Machine (FSM) for managing the game's high-level states.
 */
@Slf4j
@Singleton
@RequiredArgsConstructor
public class ApplicationStateService implements IService {
  private final ApplicationContext applicationContext;
  private final WorldService worldService;

  @Named("initial")
  private final Provider<ApplicationState> initialStateProvider;

  private final Stack<ApplicationState> stateStack = new Stack<>();

  @Override
  public int executionOrder() {
    return 100; // Must be last
  }

  public void start() {
    pushState(initialStateProvider::get);
  }

  public void stop() {
    log.info("Stopping ApplicationStateService. Popping all states.");
    while (!stateStack.isEmpty()) {
      popState();
    }
  }

  /**
   * Updates the current active state.
   *
   * @param deltaTime The time elapsed since the last frame.
   */
  public void update(float deltaTime) {
    if (!stateStack.isEmpty()) {
      stateStack.peek().onUpdate(deltaTime);
    }
  }

  public ApplicationState peek() {
    if (!stateStack.isEmpty()) {
      return stateStack.peek();
    } else {
      return null;
    }
  }

  /**
   * Pushes a new state onto the stack, making it the active state.
   *
   * @param state The new state to activate.
   */
  public void pushState(ApplicationState state) {
    log.debug("Pushing state: {}", state.getClass().getSimpleName());
    if (!stateStack.isEmpty()) {
      // Suspend the current state before covering it
      ApplicationState current = stateStack.peek();
      detachStateSystems(current);
      current.onSuspend();
    }
    stateStack.push(state);
    state.onEnter();
    attachStateSystems(state);
  }

  /** Push a state provided via Supplier; the service will call get(). */
  public void pushState(Supplier<? extends ApplicationState> supplier) {
    if (supplier == null) {
      log.warn("pushState called with null supplier");
      return;
    }
    pushState(supplier.get());
  }

  /**
   * Removes the current state from the stack, returning to the previous state.
   */
  public void popState() {
    if (!stateStack.isEmpty()) {
      ApplicationState poppedState = stateStack.pop();
      log.debug("Popping state: {}", poppedState.getClass().getSimpleName());
      detachStateSystems(poppedState);
      poppedState.onExit();
      if (!stateStack.isEmpty()) {
        // Resume the now-exposed state
        ApplicationState resume = stateStack.peek();
        resume.onResume();
        attachStateSystems(resume);
      }
    } else {
      log.warn("Attempted to pop state from an empty stack.");
    }
  }

  /**
   * Pops the current state and pushes a new one in a single, atomic operation.
   *
   * @param state The new state to activate.
   */
  public void changeState(ApplicationState state) {
    log.debug("Changing state to: {}", state.getClass().getSimpleName());
    if (!stateStack.isEmpty()) {
      ApplicationState old = stateStack.pop();
      detachStateSystems(old);
      old.onExit();
    }
    stateStack.push(state);
    state.onEnter();
    attachStateSystems(state);
  }

  /** Change state using a Supplier; the service will call get(). */
  public void changeState(Supplier<? extends ApplicationState> supplier) {
    if (supplier == null) {
      log.warn("changeState called with null supplier");
      return;
    }
    changeState(supplier.get());
  }

  /**
   * Checks if the state manager is empty.
   *
   * @return true if no states are active.
   */
  public boolean isEmpty() {
    return stateStack.isEmpty();
  }

  private void attachStateSystems(ApplicationState state) {
    if (state == null) return;
    var systems = state.systems();
    if (systems == null) return;
    for (Class<? extends engine.services.world.ISystem> cls : systems) {
      if (cls != null) {
        worldService.enableSystem(cls);
      }
    }
  }

  private void detachStateSystems(ApplicationState state) {
    if (state == null) return;
    var systems = state.systems();
    if (systems == null) return;
    for (Class<? extends engine.services.world.ISystem> cls : systems) {
      if (cls != null) {
        worldService.disableSystem(cls);
      }
    }
  }
}
