package com.grimoire.client.scene;

import com.grimoire.client.core.Window;
import com.grimoire.client.input.InputManager;
import com.grimoire.client.render.Renderer;
import com.grimoire.client.render.ui.UIRenderer;

/**
 * Base class for game scenes.
 */
public abstract class Scene {
    
    protected Window window;
    protected InputManager inputManager;
    protected Renderer renderer;
    protected UIRenderer uiRenderer;
    
    /**
     * Initializes the scene with required dependencies.
     */
    public void init(Window window, InputManager inputManager, Renderer renderer, UIRenderer uiRenderer) {
        this.window = window;
        this.inputManager = inputManager;
        this.renderer = renderer;
        this.uiRenderer = uiRenderer;
        onInit();
    }
    
    /**
     * Called when the scene is initialized.
     */
    protected abstract void onInit();
    
    /**
     * Called when the scene is entered (becomes active).
     */
    public abstract void onEnter();
    
    /**
     * Called when the scene is exited (becomes inactive).
     */
    public abstract void onExit();
    
    /**
     * Updates the scene logic.
     *
     * @param deltaTime Time since last update in seconds
     */
    public abstract void update(float deltaTime);
    
    /**
     * Renders the scene.
     */
    public abstract void render();
    
    /**
     * Called when the window is resized.
     */
    public void onResize(int width, int height) {
        // Default implementation does nothing
    }
    
    /**
     * Cleans up scene resources.
     */
    public abstract void cleanup();
}
