package engine;

public interface IService {

  /** Lifecycle start hook. */
  default void start() {}

  /** Lifecycle stop hook. */
  default void stop() {}

  /** Pre-frame update hook (runs before update(float)). */
  default void update() {}

  /** Per-frame update with delta time (runs after update()). */
  default void update(float dt) {}

  /** Execution order; lower values run earlier. */
  default int executionOrder() { return 0; }

}
