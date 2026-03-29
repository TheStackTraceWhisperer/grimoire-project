package engine.services.state;

import engine.services.world.ISystem;

import java.util.Collection;
import java.util.List;

/**
 * Represents a distinct state of the application, such as a main menu, playing, or paused.
 * Each state is responsible for its own logic and rendering.
 */
public interface ApplicationState {
  /**
   * Called once when the state becomes the active state.
   * Use this to initialize state-specific data. System enabling is handled by ApplicationStateService
   * and WorldService, based on the classes returned by systems().
   */
  void onEnter();

  /**
   * Called once per frame while this is the active state.
   *
   * @param deltaTime The time elapsed since the last frame.
   */
  void onUpdate(float deltaTime);

  /**
   * Called when the state is about to be exited.
   * Use this for cleanup before transitioning to a new state.
   */
  void onExit();

  /**
   * Called when the state is uncovered and becomes active again after another
   * state above it has been popped. Default no-op.
   */
  default void onResume() { }

  /**
   * Called when the state is being covered by another pushed state. Default no-op.
   */
  default void onSuspend() { }

  /**
   * Declares the set of world system classes this state needs when active. ApplicationStateService
   * will request WorldService to enable them before calling onEnter/onResume, and disable them on suspend/exit.
   * Default is an empty list.
   */
  default Collection<Class<? extends ISystem>> systems() { return List.of(); }
}
