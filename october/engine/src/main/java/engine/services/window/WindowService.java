package engine.services.window;

import engine.IService;
import engine.services.rendering.gl.OpenGLDebugger;
import jakarta.inject.Singleton;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.IntBuffer;

/** GLFW window wrapper that creates an OpenGL 4.6 core context. */
@Singleton
@RequiredArgsConstructor
public final class WindowService implements IService {
  private static final Logger log = LoggerFactory.getLogger(WindowService.class);

  private final WindowDefaults defaults;

  @Getter
  private long handle = 0L;

  @Override
  public int executionOrder() {
    return Integer.MIN_VALUE + 1;
  }

  public void start() {
    int width = defaults.width();
    int height = defaults.height();
    String title = defaults.title();

    GLFW.glfwDefaultWindowHints();
    GLFW.glfwWindowHint(GLFW.GLFW_CLIENT_API, GLFW.GLFW_OPENGL_API);
    GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 4);
    GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 6);
    GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE);
    GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_TRUE);
    GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE);

    log.info("Creating GLFW window with OpenGL 4.6 core profile");
    long window = GLFW.glfwCreateWindow(width, height, title, 0L, 0L);
    if (window == 0L) {
      throw new IllegalStateException(
        "Unable to create GLFW window (requested OpenGL 4.6 core profile)");
    }

    this.handle = window;

    GLFW.glfwMakeContextCurrent(window);
    if (GLFW.glfwGetCurrentContext() != window) {
      GLFW.glfwDestroyWindow(window);
      this.handle = 0L;
      throw new IllegalStateException("Failed to make OpenGL context current");
    }

    try {
      GL.createCapabilities();
      OpenGLDebugger.init();
      GLFW.glfwSwapInterval(1);
      GL30.glViewport(0, 0, width, height);

      try {
        String glVersion = GL11.glGetString(GL11.GL_VERSION);
        String glRenderer = GL11.glGetString(GL11.GL_RENDERER);
        String glVendor = GL11.glGetString(GL11.GL_VENDOR);
        log.info("OpenGL reported version: {}", glVersion);
        log.info("OpenGL renderer: {}", glRenderer);
        log.info("OpenGL vendor: {}", glVendor);
      } catch (Throwable t) {
        log.warn("Failed to query OpenGL version information", t);
      }
    } catch (IllegalStateException ise) {
      GLFW.glfwDestroyWindow(window);
      this.handle = 0L;
      throw ise;
    }

    GLFW.glfwShowWindow(window);
    log.info("Created GLFW window: handle={}", window);
  }

  public int getWidth() {
    try (MemoryStack stack = MemoryStack.stackPush()) {
      IntBuffer pWidth = stack.mallocInt(1);
      IntBuffer pHeight = stack.mallocInt(1);
      GLFW.glfwGetWindowSize(handle, pWidth, pHeight);
      return pWidth.get(0);
    }
  }

  public int getHeight() {
    try (MemoryStack stack = MemoryStack.stackPush()) {
      IntBuffer pWidth = stack.mallocInt(1);
      IntBuffer pHeight = stack.mallocInt(1);
      GLFW.glfwGetWindowSize(handle, pWidth, pHeight);
      return pHeight.get(0);
    }
  }

  public void swapBuffers() {
    GLFW.glfwSwapBuffers(handle);
  }

  /** Process pending window events. */
  public void pollEvents() {
    GLFW.glfwPollEvents();
  }

  /** Set a resize listener; also updates GL viewport. */
  public void setResizeListener(WindowResizeListener listener) {
    GLFW.glfwSetFramebufferSizeCallback(
      handle,
      (win, w, h) -> {
        if (w > 0 && h > 0) {
          GL30.glViewport(0, 0, w, h);
          if (listener != null) {
            listener.onResize(w, h);
          }
        }
      });
  }

  public void stop() {
    if (handle != 0L) {
      log.info("Destroying GLFW window: handle={}", handle);
      GLFW.glfwDestroyWindow(handle);
      handle = 0L;
    }
    OpenGLDebugger.cleanup();
  }

}
