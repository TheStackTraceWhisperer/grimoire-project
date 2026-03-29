package com.grimoire.clientv2.render;

import com.grimoire.clientv2.core.Window;
import com.grimoire.clientv2.render.mesh.Mesh;
import com.grimoire.clientv2.render.shader.ShaderProgram;
import com.grimoire.clientv2.render.texture.Texture;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.joml.Matrix4f;
import org.joml.Vector4f;

import java.io.IOException;

import static org.lwjgl.opengl.GL45.*;

/**
 * Main renderer for the game.
 * Handles world and entity rendering using OpenGL 4.5 core profile.
 */
@Slf4j
public class Renderer {
    
    private ShaderProgram basicShader;
    private Mesh quadMesh;
    private Texture whiteTexture;
    
    @Getter
    private Camera worldCamera;
    
    @Getter
    private Camera uiCamera;
    
    private final Matrix4f modelMatrix = new Matrix4f();
    
    private int windowWidth;
    private int windowHeight;
    
    /**
     * Initializes the renderer with default shaders and resources.
     */
    public void init(Window window) throws IOException {
        this.windowWidth = window.getWidth();
        this.windowHeight = window.getHeight();
        
        // Create basic shader
        basicShader = new ShaderProgram();
        basicShader.createVertexShader(ShaderProgram.loadResource("/shaders/basic.vert"));
        basicShader.createFragmentShader(ShaderProgram.loadResource("/shaders/basic.frag"));
        basicShader.link();
        
        basicShader.createUniform("projectionMatrix");
        basicShader.createUniform("viewMatrix");
        basicShader.createUniform("modelMatrix");
        basicShader.createUniform("color");
        basicShader.createUniform("textureSampler");
        basicShader.createUniformOptional("useTexture");
        
        // Create default quad mesh
        quadMesh = Mesh.createQuad(1.0f, 1.0f);
        
        // Create white texture for colored rendering
        whiteTexture = Texture.createWhite();
        
        // Setup cameras
        worldCamera = new Camera(windowWidth / 2.0f, windowHeight / 2.0f, 100.0f);
        worldCamera.setOrthographic(windowWidth, windowHeight);
        
        uiCamera = Camera.createScreenCamera(windowWidth, windowHeight);
        
        log.info("Renderer initialized");
    }
    
    /**
     * Handles window resize.
     */
    public void resize(int width, int height) {
        this.windowWidth = width;
        this.windowHeight = height;
        
        glViewport(0, 0, width, height);
        
        worldCamera.setOrthographic(width, height);
        worldCamera.setPosition(width / 2.0f, height / 2.0f, 100.0f);
        
        uiCamera = Camera.createScreenCamera(width, height);
    }
    
    /**
     * Begins a new frame (clears the screen).
     */
    public void beginFrame() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    }
    
    /**
     * Ends the frame.
     */
    public void endFrame() {
        // Nothing special needed for basic rendering
    }
    
    /**
     * Begins world-space rendering.
     */
    public void beginWorldRendering() {
        basicShader.bind();
        basicShader.setUniform("projectionMatrix", worldCamera.getProjectionMatrix());
        basicShader.setUniform("viewMatrix", worldCamera.getViewMatrix());
        basicShader.setUniform("textureSampler", 0);
    }
    
    /**
     * Ends world-space rendering.
     */
    public void endWorldRendering() {
        basicShader.unbind();
    }
    
    /**
     * Begins UI-space rendering (disables depth test).
     */
    public void beginUIRendering() {
        glDisable(GL_DEPTH_TEST);
        basicShader.bind();
        basicShader.setUniform("projectionMatrix", uiCamera.getProjectionMatrix());
        basicShader.setUniform("viewMatrix", uiCamera.getViewMatrix());
        basicShader.setUniform("textureSampler", 0);
    }
    
    /**
     * Ends UI-space rendering.
     */
    public void endUIRendering() {
        basicShader.unbind();
        glEnable(GL_DEPTH_TEST);
    }
    
    /**
     * Renders a colored quad at the specified position.
     *
     * @param x      X position
     * @param y      Y position
     * @param width  Width
     * @param height Height
     * @param color  Color (RGBA)
     */
    public void renderQuad(float x, float y, float width, float height, Vector4f color) {
        modelMatrix.identity()
                .translate(x + width / 2.0f, y + height / 2.0f, 0)
                .scale(width, height, 1);
        
        basicShader.setUniform("modelMatrix", modelMatrix);
        basicShader.setUniform("color", color);
        basicShader.setUniform("useTexture", 0);
        
        whiteTexture.bind();
        quadMesh.render();
    }
    
    /**
     * Renders a textured quad at the specified position.
     *
     * @param x       X position
     * @param y       Y position
     * @param width   Width
     * @param height  Height
     * @param texture The texture to use
     */
    public void renderTexturedQuad(float x, float y, float width, float height, Texture texture) {
        renderTexturedQuad(x, y, width, height, texture, new Vector4f(1, 1, 1, 1));
    }
    
    /**
     * Renders a textured quad with color tinting.
     */
    public void renderTexturedQuad(float x, float y, float width, float height, Texture texture, Vector4f color) {
        modelMatrix.identity()
                .translate(x + width / 2.0f, y + height / 2.0f, 0)
                .scale(width, height, 1);
        
        basicShader.setUniform("modelMatrix", modelMatrix);
        basicShader.setUniform("color", color);
        basicShader.setUniform("useTexture", 1);
        
        texture.bind();
        quadMesh.render();
    }
    
    /**
     * Renders a circle (approximated with a quad for now).
     */
    public void renderCircle(float x, float y, float radius, Vector4f color) {
        // For simplicity, render as a quad - could implement actual circle rendering
        renderQuad(x - radius, y - radius, radius * 2, radius * 2, color);
    }
    
    /**
     * Cleans up renderer resources.
     */
    public void cleanup() {
        if (basicShader != null) basicShader.cleanup();
        if (quadMesh != null) quadMesh.cleanup();
        if (whiteTexture != null) whiteTexture.cleanup();
    }
    
    /**
     * Returns the window width.
     */
    public int getWindowWidth() {
        return windowWidth;
    }
    
    /**
     * Returns the window height.
     */
    public int getWindowHeight() {
        return windowHeight;
    }
}
