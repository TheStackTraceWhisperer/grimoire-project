package engine;

import org.lwjgl.glfw.GLFW;

import java.time.Duration;

public interface ApplicationLoopPolicy {
  boolean continueRunning(int frames, long windowHandle);

  /** Continue until the window requests close. */
  static ApplicationLoopPolicy standard() {
    return (f, h) -> !GLFW.glfwWindowShouldClose(h);
  }

  /** Limit by frame count (0 => no frames). */
  static ApplicationLoopPolicy frames(int maxFrames) {
    return (f, h) -> f < maxFrames;
  }

  /** Alias for frames(0). */
  static ApplicationLoopPolicy skip() {
    return frames(0);
  }

  /** Time limited and window-open requirement. */
  static ApplicationLoopPolicy timed(Duration duration) {
    final long limitNanos = duration.toNanos();
    final long start = System.nanoTime();
    if (limitNanos == 0L) {
      return (f, h) -> false;
    }
    return (f, h) -> !GLFW.glfwWindowShouldClose(h) && (System.nanoTime() - start) < limitNanos;
  }

  /** Logical AND; empty => true. */
  static ApplicationLoopPolicy all(ApplicationLoopPolicy... policies) {
    return (f, h) -> {
      for (ApplicationLoopPolicy p : policies)
        if (!p.continueRunning(f, h)) {
          return false;
        }
      return true;
    };
  }

  /** Logical OR; empty => false. */
  static ApplicationLoopPolicy any(ApplicationLoopPolicy... policies) {
    return (f, h) -> {
      for (ApplicationLoopPolicy p : policies)
        if (p.continueRunning(f, h)) {
          return true;
        }
      return false;
    };
  }
}
