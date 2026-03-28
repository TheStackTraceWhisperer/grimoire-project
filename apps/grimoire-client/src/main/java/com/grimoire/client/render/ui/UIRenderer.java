package com.grimoire.client.render.ui;

import com.grimoire.client.render.shader.ShaderProgram;
import com.grimoire.client.render.texture.Texture;
import lombok.extern.slf4j.Slf4j;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryUtil;

import java.io.IOException;
import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL45.*;

/**
 * UI-specific renderer using batch rendering for efficiency.
 * Provides immediate-mode style rendering API while batching draw calls.
 */
@Slf4j
public class UIRenderer {
    
    private static final int MAX_QUADS = 10000;
    private static final int VERTICES_PER_QUAD = 4;
    private static final int INDICES_PER_QUAD = 6;
    private static final int FLOATS_PER_VERTEX = 8; // x, y, u, v, r, g, b, a
    
    private ShaderProgram shader;
    private Texture whiteTexture;
    private FontRenderer fontRenderer;
    
    private int vao;
    private int vbo;
    private int ebo;
    
    private FloatBuffer vertexBuffer;
    private int quadCount;
    
    private final Matrix4f projectionMatrix = new Matrix4f();
    private int screenWidth;
    private int screenHeight;
    
    private Texture currentTexture;
    private boolean isTextMode;
    
    /**
     * Initializes the UI renderer.
     */
    public void init(int screenWidth, int screenHeight) throws IOException {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        
        // Create shader
        shader = new ShaderProgram();
        shader.createVertexShader(ShaderProgram.loadResource("/shaders/ui.vert"));
        shader.createFragmentShader(ShaderProgram.loadResource("/shaders/ui.frag"));
        shader.link();
        
        shader.createUniform("projectionMatrix");
        shader.createUniform("textureSampler");
        shader.createUniform("isText");
        
        // Create white texture
        whiteTexture = Texture.createWhite();
        
        // Initialize font renderer
        fontRenderer = new FontRenderer();
        fontRenderer.init();
        
        // Create VAO and buffers
        vao = glCreateVertexArrays();
        glBindVertexArray(vao);
        
        // Create and configure VBO
        vbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, (long) MAX_QUADS * VERTICES_PER_QUAD * FLOATS_PER_VERTEX * Float.BYTES, GL_DYNAMIC_DRAW);
        
        // Position (2 floats)
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(0, 2, GL_FLOAT, false, FLOATS_PER_VERTEX * Float.BYTES, 0);
        
        // Texture coordinates (2 floats)
        glEnableVertexAttribArray(1);
        glVertexAttribPointer(1, 2, GL_FLOAT, false, FLOATS_PER_VERTEX * Float.BYTES, 2L * Float.BYTES);
        
        // Color (4 floats)
        glEnableVertexAttribArray(2);
        glVertexAttribPointer(2, 4, GL_FLOAT, false, FLOATS_PER_VERTEX * Float.BYTES, 4L * Float.BYTES);
        
        // Create index buffer
        int[] indices = generateIndices();
        ebo = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW);
        
        glBindVertexArray(0);
        
        // Allocate vertex buffer
        vertexBuffer = MemoryUtil.memAllocFloat(MAX_QUADS * VERTICES_PER_QUAD * FLOATS_PER_VERTEX);
        
        updateProjection();
        
        log.info("UI Renderer initialized");
    }
    
    private int[] generateIndices() {
        int[] indices = new int[MAX_QUADS * INDICES_PER_QUAD];
        int offset = 0;
        for (int i = 0; i < MAX_QUADS; i++) {
            indices[i * 6] = offset;
            indices[i * 6 + 1] = offset + 1;
            indices[i * 6 + 2] = offset + 2;
            indices[i * 6 + 3] = offset + 2;
            indices[i * 6 + 4] = offset + 3;
            indices[i * 6 + 5] = offset;
            offset += 4;
        }
        return indices;
    }
    
    /**
     * Updates the projection matrix for window resize.
     */
    public void resize(int width, int height) {
        this.screenWidth = width;
        this.screenHeight = height;
        updateProjection();
    }
    
    private void updateProjection() {
        // Orthographic projection with origin at top-left
        projectionMatrix.identity().ortho(0, screenWidth, screenHeight, 0, -1, 1);
    }
    
    /**
     * Begins a new UI rendering batch.
     */
    public void begin() {
        quadCount = 0;
        vertexBuffer.clear();
        currentTexture = whiteTexture;
        isTextMode = false;
        
        glDisable(GL_DEPTH_TEST);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        
        shader.bind();
        shader.setUniform("projectionMatrix", projectionMatrix);
        shader.setUniform("textureSampler", 0);
        shader.setUniform("isText", 0);
    }
    
    /**
     * Ends the UI rendering batch and flushes all pending draws.
     */
    public void end() {
        flush();
        shader.unbind();
        glEnable(GL_DEPTH_TEST);
    }
    
    /**
     * Flushes the current batch to the GPU.
     */
    public void flush() {
        if (quadCount == 0) return;
        
        vertexBuffer.flip();
        
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferSubData(GL_ARRAY_BUFFER, 0, vertexBuffer);
        
        currentTexture.bind();
        
        glBindVertexArray(vao);
        glDrawElements(GL_TRIANGLES, quadCount * INDICES_PER_QUAD, GL_UNSIGNED_INT, 0);
        glBindVertexArray(0);
        
        quadCount = 0;
        vertexBuffer.clear();
    }
    
    private void checkFlush(Texture texture, boolean textMode) {
        if (texture != currentTexture || textMode != isTextMode || quadCount >= MAX_QUADS) {
            flush();
            currentTexture = texture;
            isTextMode = textMode;
            shader.setUniform("isText", textMode ? 1 : 0);
        }
    }
    
    /**
     * Renders a colored quad.
     */
    public void renderQuad(float x, float y, float width, float height, Vector4f color) {
        checkFlush(whiteTexture, false);
        
        float x2 = x + width;
        float y2 = y + height;
        
        // Top-left
        addVertex(x, y, 0, 0, color);
        // Top-right
        addVertex(x2, y, 1, 0, color);
        // Bottom-right
        addVertex(x2, y2, 1, 1, color);
        // Bottom-left
        addVertex(x, y2, 0, 1, color);
        
        quadCount++;
    }
    
    /**
     * Renders a textured quad.
     */
    public void renderTexturedQuad(float x, float y, float width, float height, 
                                    Texture texture, Vector4f color) {
        checkFlush(texture, false);
        
        float x2 = x + width;
        float y2 = y + height;
        
        addVertex(x, y, 0, 0, color);
        addVertex(x2, y, 1, 0, color);
        addVertex(x2, y2, 1, 1, color);
        addVertex(x, y2, 0, 1, color);
        
        quadCount++;
    }
    
    /**
     * Renders a textured quad with custom UV coordinates.
     */
    public void renderTexturedQuad(float x, float y, float width, float height,
                                    Texture texture, float u1, float v1, float u2, float v2,
                                    Vector4f color) {
        checkFlush(texture, false);
        
        float x2 = x + width;
        float y2 = y + height;
        
        addVertex(x, y, u1, v1, color);
        addVertex(x2, y, u2, v1, color);
        addVertex(x2, y2, u2, v2, color);
        addVertex(x, y2, u1, v2, color);
        
        quadCount++;
    }
    
    private void addVertex(float x, float y, float u, float v, Vector4f color) {
        vertexBuffer.put(x).put(y).put(u).put(v);
        vertexBuffer.put(color.x).put(color.y).put(color.z).put(color.w);
    }
    
    /**
     * Renders text at the specified position.
     */
    public void renderText(String text, float x, float y, float scale, Vector4f color) {
        fontRenderer.renderText(this, text, x, y, scale, color);
    }
    
    /**
     * Renders text centered within a bounding box.
     */
    public void renderTextCentered(String text, float x, float y, float width, float height, 
                                    float scale, Vector4f color) {
        float textWidth = fontRenderer.getTextWidth(text, scale);
        float textHeight = fontRenderer.getTextHeight(scale);
        
        float textX = x + (width - textWidth) / 2;
        float textY = y + (height - textHeight) / 2;
        
        renderText(text, textX, textY, scale, color);
    }
    
    /**
     * Gets the font renderer for text measurements.
     */
    public FontRenderer getFontRenderer() {
        return fontRenderer;
    }
    
    /**
     * Cleans up renderer resources.
     */
    public void cleanup() {
        if (shader != null) shader.cleanup();
        if (whiteTexture != null) whiteTexture.cleanup();
        if (fontRenderer != null) fontRenderer.cleanup();
        
        glDeleteBuffers(vbo);
        glDeleteBuffers(ebo);
        glDeleteVertexArrays(vao);
        
        if (vertexBuffer != null) {
            MemoryUtil.memFree(vertexBuffer);
        }
    }
}
