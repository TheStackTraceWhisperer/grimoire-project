package com.grimoire.clientv2.input;

import lombok.Getter;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;
import org.lwjgl.glfw.GLFWScrollCallback;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.glfw.GLFW.*;

/**
 * Input manager for handling keyboard and mouse input.
 */
public class InputManager {
    
    private static final int MAX_KEYS = 512;
    private static final int MAX_BUTTONS = 8;
    
    private final boolean[] keys = new boolean[MAX_KEYS];
    private final boolean[] keysPressed = new boolean[MAX_KEYS];
    private final boolean[] keysReleased = new boolean[MAX_KEYS];
    
    private final boolean[] mouseButtons = new boolean[MAX_BUTTONS];
    private final boolean[] mouseButtonsPressed = new boolean[MAX_BUTTONS];
    private final boolean[] mouseButtonsReleased = new boolean[MAX_BUTTONS];
    
    @Getter
    private double mouseX;
    
    @Getter
    private double mouseY;
    
    @Getter
    private double scrollX;
    
    @Getter
    private double scrollY;
    
    private double lastMouseX;
    private double lastMouseY;
    
    @Getter
    private double mouseDeltaX;
    
    @Getter
    private double mouseDeltaY;
    
    private final List<KeyListener> keyListeners = new ArrayList<>();
    private final List<MouseListener> mouseListeners = new ArrayList<>();
    
    private GLFWKeyCallback keyCallback;
    private GLFWMouseButtonCallback mouseButtonCallback;
    private GLFWCursorPosCallback cursorPosCallback;
    private GLFWScrollCallback scrollCallback;
    
    /**
     * Initializes input callbacks for the given window.
     */
    public void init(long windowHandle) {
        // Key callback
        keyCallback = glfwSetKeyCallback(windowHandle, (window, key, scancode, action, mods) -> {
            if (key >= 0 && key < MAX_KEYS) {
                if (action == GLFW_PRESS) {
                    keys[key] = true;
                    keysPressed[key] = true;
                    notifyKeyPressed(key, mods);
                } else if (action == GLFW_RELEASE) {
                    keys[key] = false;
                    keysReleased[key] = true;
                    notifyKeyReleased(key, mods);
                }
            }
        });
        
        // Mouse button callback
        mouseButtonCallback = glfwSetMouseButtonCallback(windowHandle, (window, button, action, mods) -> {
            if (button >= 0 && button < MAX_BUTTONS) {
                if (action == GLFW_PRESS) {
                    mouseButtons[button] = true;
                    mouseButtonsPressed[button] = true;
                    notifyMousePressed(button, mouseX, mouseY);
                } else if (action == GLFW_RELEASE) {
                    mouseButtons[button] = false;
                    mouseButtonsReleased[button] = true;
                    notifyMouseReleased(button, mouseX, mouseY);
                }
            }
        });
        
        // Cursor position callback
        cursorPosCallback = glfwSetCursorPosCallback(windowHandle, (window, xpos, ypos) -> {
            mouseX = xpos;
            mouseY = ypos;
            notifyMouseMoved(xpos, ypos);
        });
        
        // Scroll callback
        scrollCallback = glfwSetScrollCallback(windowHandle, (window, xoffset, yoffset) -> {
            scrollX = xoffset;
            scrollY = yoffset;
            notifyMouseScrolled(xoffset, yoffset);
        });
    }
    
    /**
     * Updates input state (call at the end of each frame).
     */
    public void update() {
        // Calculate mouse delta
        mouseDeltaX = mouseX - lastMouseX;
        mouseDeltaY = mouseY - lastMouseY;
        lastMouseX = mouseX;
        lastMouseY = mouseY;
        
        // Clear single-frame states
        for (int i = 0; i < MAX_KEYS; i++) {
            keysPressed[i] = false;
            keysReleased[i] = false;
        }
        for (int i = 0; i < MAX_BUTTONS; i++) {
            mouseButtonsPressed[i] = false;
            mouseButtonsReleased[i] = false;
        }
        
        scrollX = 0;
        scrollY = 0;
    }
    
    /**
     * Checks if a key is currently held down.
     */
    public boolean isKeyDown(int key) {
        return key >= 0 && key < MAX_KEYS && keys[key];
    }
    
    /**
     * Checks if a key was just pressed this frame.
     */
    public boolean isKeyPressed(int key) {
        return key >= 0 && key < MAX_KEYS && keysPressed[key];
    }
    
    /**
     * Checks if a key was just released this frame.
     */
    public boolean isKeyReleased(int key) {
        return key >= 0 && key < MAX_KEYS && keysReleased[key];
    }
    
    /**
     * Checks if a mouse button is currently held down.
     */
    public boolean isMouseButtonDown(int button) {
        return button >= 0 && button < MAX_BUTTONS && mouseButtons[button];
    }
    
    /**
     * Checks if a mouse button was just pressed this frame.
     */
    public boolean isMouseButtonPressed(int button) {
        return button >= 0 && button < MAX_BUTTONS && mouseButtonsPressed[button];
    }
    
    /**
     * Checks if a mouse button was just released this frame.
     */
    public boolean isMouseButtonReleased(int button) {
        return button >= 0 && button < MAX_BUTTONS && mouseButtonsReleased[button];
    }
    
    /**
     * Adds a key listener.
     */
    public void addKeyListener(KeyListener listener) {
        keyListeners.add(listener);
    }
    
    /**
     * Removes a key listener.
     */
    public void removeKeyListener(KeyListener listener) {
        keyListeners.remove(listener);
    }
    
    /**
     * Adds a mouse listener.
     */
    public void addMouseListener(MouseListener listener) {
        mouseListeners.add(listener);
    }
    
    /**
     * Removes a mouse listener.
     */
    public void removeMouseListener(MouseListener listener) {
        mouseListeners.remove(listener);
    }
    
    private void notifyKeyPressed(int key, int mods) {
        for (KeyListener listener : keyListeners) {
            listener.keyPressed(key, mods);
        }
    }
    
    private void notifyKeyReleased(int key, int mods) {
        for (KeyListener listener : keyListeners) {
            listener.keyReleased(key, mods);
        }
    }
    
    private void notifyMousePressed(int button, double x, double y) {
        for (MouseListener listener : mouseListeners) {
            listener.mousePressed(button, x, y);
        }
    }
    
    private void notifyMouseReleased(int button, double x, double y) {
        for (MouseListener listener : mouseListeners) {
            listener.mouseReleased(button, x, y);
        }
    }
    
    private void notifyMouseMoved(double x, double y) {
        for (MouseListener listener : mouseListeners) {
            listener.mouseMoved(x, y);
        }
    }
    
    private void notifyMouseScrolled(double xOffset, double yOffset) {
        for (MouseListener listener : mouseListeners) {
            listener.mouseScrolled(xOffset, yOffset);
        }
    }
    
    /**
     * Cleans up input callbacks.
     */
    public void cleanup() {
        if (keyCallback != null) keyCallback.free();
        if (mouseButtonCallback != null) mouseButtonCallback.free();
        if (cursorPosCallback != null) cursorPosCallback.free();
        if (scrollCallback != null) scrollCallback.free();
    }
    
    /**
     * Keyboard event listener interface.
     */
    public interface KeyListener {
        void keyPressed(int key, int mods);
        void keyReleased(int key, int mods);
    }
    
    /**
     * Mouse event listener interface.
     */
    public interface MouseListener {
        void mousePressed(int button, double x, double y);
        void mouseReleased(int button, double x, double y);
        void mouseMoved(double x, double y);
        void mouseScrolled(double xOffset, double yOffset);
    }
    
    /**
     * Adapter class for KeyListener.
     */
    public static class KeyAdapter implements KeyListener {
        @Override
        public void keyPressed(int key, int mods) {}
        
        @Override
        public void keyReleased(int key, int mods) {}
    }
    
    /**
     * Adapter class for MouseListener.
     */
    public static class MouseAdapter implements MouseListener {
        @Override
        public void mousePressed(int button, double x, double y) {}
        
        @Override
        public void mouseReleased(int button, double x, double y) {}
        
        @Override
        public void mouseMoved(double x, double y) {}
        
        @Override
        public void mouseScrolled(double xOffset, double yOffset) {}
    }
}
