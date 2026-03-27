package com.grimoire.clientv2.core;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL45.*;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;

/**
 * GLFW window wrapper for the LWJGL-based client.
 * Manages window creation, OpenGL context, and basic input handling.
 */
@Slf4j
public class Window {
    
    @Getter
    private final String title;
    
    @Getter
    private int width;
    
    @Getter
    private int height;
    
    private long windowHandle;
    
    @Getter
    private boolean resized;
    
    private boolean vSync;
    
    /**
     * Creates a new window instance.
     *
     * @param title  Window title
     * @param width  Initial width
     * @param height Initial height
     * @param vSync  Enable vertical sync
     */
    public Window(String title, int width, int height, boolean vSync) {
        this.title = title;
        this.width = width;
        this.height = height;
        this.vSync = vSync;
        this.resized = false;
    }
    
    /**
     * Initializes GLFW and creates the window with OpenGL 4.5 core profile context.
     */
    public void init() {
        // Setup error callback
        GLFWErrorCallback.createPrint(System.err).set();
        
        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }
        
        // Configure GLFW - OpenGL 4.5 Core Profile
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 4);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 5);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_TRUE);
        
        // Create window
        windowHandle = glfwCreateWindow(width, height, title, NULL, NULL);
        if (windowHandle == NULL) {
            throw new RuntimeException("Failed to create the GLFW window");
        }
        
        // Setup resize callback
        glfwSetFramebufferSizeCallback(windowHandle, (window, w, h) -> {
            this.width = w;
            this.height = h;
            this.resized = true;
        });
        
        // Setup key callback for escape to close
        glfwSetKeyCallback(windowHandle, (window, key, scancode, action, mods) -> {
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
                glfwSetWindowShouldClose(window, true);
            }
        });
        
        // Center window
        try (MemoryStack stack = stackPush()) {
            IntBuffer pWidth = stack.mallocInt(1);
            IntBuffer pHeight = stack.mallocInt(1);
            
            glfwGetWindowSize(windowHandle, pWidth, pHeight);
            
            GLFWVidMode vidMode = glfwGetVideoMode(glfwGetPrimaryMonitor());
            if (vidMode != null) {
                glfwSetWindowPos(
                        windowHandle,
                        (vidMode.width() - pWidth.get(0)) / 2,
                        (vidMode.height() - pHeight.get(0)) / 2
                );
            }
        }
        
        // Make OpenGL context current
        glfwMakeContextCurrent(windowHandle);
        
        // Enable v-sync
        if (vSync) {
            glfwSwapInterval(1);
        }
        
        // Make window visible
        glfwShowWindow(windowHandle);
        
        // Initialize OpenGL
        GL.createCapabilities();
        
        // Enable depth testing and blending
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        
        // Set clear color
        glClearColor(0.1f, 0.1f, 0.15f, 1.0f);
        
        log.info("OpenGL initialized: {}", glGetString(GL_VERSION));
        log.info("Renderer: {}", glGetString(GL_RENDERER));
    }
    
    /**
     * Returns the native GLFW window handle.
     */
    public long getHandle() {
        return windowHandle;
    }
    
    /**
     * Checks if the window should close.
     */
    public boolean shouldClose() {
        return glfwWindowShouldClose(windowHandle);
    }
    
    /**
     * Sets whether the window should close.
     */
    public void setShouldClose(boolean value) {
        glfwSetWindowShouldClose(windowHandle, value);
    }
    
    /**
     * Updates the window (swap buffers and poll events).
     */
    public void update() {
        glfwSwapBuffers(windowHandle);
        glfwPollEvents();
    }
    
    /**
     * Clears the resized flag.
     */
    public void clearResizedFlag() {
        this.resized = false;
    }
    
    /**
     * Returns the aspect ratio of the window.
     */
    public float getAspectRatio() {
        return (float) width / (float) height;
    }
    
    /**
     * Checks if a key is pressed.
     */
    public boolean isKeyPressed(int keyCode) {
        return glfwGetKey(windowHandle, keyCode) == GLFW_PRESS;
    }
    
    /**
     * Cleans up GLFW resources.
     */
    public void cleanup() {
        glfwFreeCallbacks(windowHandle);
        glfwDestroyWindow(windowHandle);
        glfwTerminate();
        
        GLFWErrorCallback callback = glfwSetErrorCallback(null);
        if (callback != null) {
            callback.free();
        }
    }
}
