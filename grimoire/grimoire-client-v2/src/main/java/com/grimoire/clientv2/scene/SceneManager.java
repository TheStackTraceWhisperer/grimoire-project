package com.grimoire.clientv2.scene;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages game scenes and transitions.
 */
@Slf4j
public class SceneManager {
    
    private final Map<String, Scene> scenes = new HashMap<>();
    
    @Getter
    private Scene currentScene;
    
    @Getter
    private String currentSceneName;
    
    /**
     * Registers a scene with the manager.
     */
    public void registerScene(String name, Scene scene) {
        scenes.put(name, scene);
        log.debug("Registered scene: {}", name);
    }
    
    /**
     * Switches to a different scene.
     */
    public void switchTo(String sceneName) {
        Scene newScene = scenes.get(sceneName);
        if (newScene == null) {
            throw new IllegalArgumentException("Scene not found: " + sceneName);
        }
        
        if (currentScene != null) {
            log.debug("Exiting scene: {}", currentSceneName);
            currentScene.onExit();
        }
        
        currentScene = newScene;
        currentSceneName = sceneName;
        
        log.info("Entering scene: {}", sceneName);
        currentScene.onEnter();
    }
    
    /**
     * Updates the current scene.
     */
    public void update(float deltaTime) {
        if (currentScene != null) {
            currentScene.update(deltaTime);
        }
    }
    
    /**
     * Renders the current scene.
     */
    public void render() {
        if (currentScene != null) {
            currentScene.render();
        }
    }
    
    /**
     * Handles window resize.
     */
    public void onResize(int width, int height) {
        if (currentScene != null) {
            currentScene.onResize(width, height);
        }
    }
    
    /**
     * Cleans up all scenes.
     */
    public void cleanup() {
        for (Scene scene : scenes.values()) {
            scene.cleanup();
        }
        scenes.clear();
    }
}
