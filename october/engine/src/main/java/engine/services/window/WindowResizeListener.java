package engine.services.window;

/** Listener for window framebuffer resize events. */
@FunctionalInterface
public interface WindowResizeListener {
  /** Called when framebuffer is resized. */
  void onResize(int width, int height);
}
