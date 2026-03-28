package com.grimoire.client.render.texture;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.lwjgl.system.MemoryStack;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL45.*;
import static org.lwjgl.stb.STBImage.*;

/**
 * OpenGL texture wrapper using STB for image loading.
 */
@Slf4j
public class Texture {
    
    @Getter
    private final int textureId;
    
    @Getter
    private final int width;
    
    @Getter
    private final int height;
    
    /**
     * Creates a texture from raw data.
     *
     * @param width    Texture width
     * @param height   Texture height
     * @param buffer   Pixel data (RGBA format)
     */
    public Texture(int width, int height, ByteBuffer buffer) {
        this.width = width;
        this.height = height;
        
        textureId = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, textureId);
        
        // Set texture parameters
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        
        // Upload texture data
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);
        glGenerateMipmap(GL_TEXTURE_2D);
        
        glBindTexture(GL_TEXTURE_2D, 0);
    }
    
    /**
     * Creates an empty texture for use as a render target.
     *
     * @param width  Texture width
     * @param height Texture height
     */
    public Texture(int width, int height) {
        this.width = width;
        this.height = height;
        
        textureId = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, textureId);
        
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, (ByteBuffer) null);
        
        glBindTexture(GL_TEXTURE_2D, 0);
    }
    
    /**
     * Loads a texture from a resource path.
     *
     * @param resourcePath Path to the texture resource
     * @return The loaded texture
     */
    public static Texture load(String resourcePath) throws IOException {
        ByteBuffer imageBuffer;
        int width;
        int height;
        
        try (InputStream is = Texture.class.getResourceAsStream(resourcePath)) {
            if (is == null) {
                throw new IOException("Resource not found: " + resourcePath);
            }
            
            byte[] imageData = is.readAllBytes();
            ByteBuffer buffer = org.lwjgl.system.MemoryUtil.memAlloc(imageData.length);
            buffer.put(imageData).flip();
            
            try (MemoryStack stack = MemoryStack.stackPush()) {
                IntBuffer w = stack.mallocInt(1);
                IntBuffer h = stack.mallocInt(1);
                IntBuffer channels = stack.mallocInt(1);
                
                stbi_set_flip_vertically_on_load(true);
                
                imageBuffer = stbi_load_from_memory(buffer, w, h, channels, 4);
                if (imageBuffer == null) {
                    throw new IOException("Failed to load texture: " + stbi_failure_reason());
                }
                
                width = w.get(0);
                height = h.get(0);
            } finally {
                org.lwjgl.system.MemoryUtil.memFree(buffer);
            }
        }
        
        Texture texture = new Texture(width, height, imageBuffer);
        stbi_image_free(imageBuffer);
        
        log.debug("Loaded texture: {} ({}x{})", resourcePath, width, height);
        return texture;
    }
    
    /**
     * Creates a solid color texture.
     *
     * @param r Red component (0-255)
     * @param g Green component (0-255)
     * @param b Blue component (0-255)
     * @param a Alpha component (0-255)
     * @return A 1x1 texture with the specified color
     */
    public static Texture createSolidColor(int r, int g, int b, int a) {
        ByteBuffer buffer = org.lwjgl.system.MemoryUtil.memAlloc(4);
        buffer.put((byte) r).put((byte) g).put((byte) b).put((byte) a).flip();
        Texture texture = new Texture(1, 1, buffer);
        org.lwjgl.system.MemoryUtil.memFree(buffer);
        return texture;
    }
    
    /**
     * Creates a white texture.
     */
    public static Texture createWhite() {
        return createSolidColor(255, 255, 255, 255);
    }
    
    /**
     * Binds the texture to a texture unit.
     *
     * @param unit The texture unit (0-15)
     */
    public void bind(int unit) {
        glActiveTexture(GL_TEXTURE0 + unit);
        glBindTexture(GL_TEXTURE_2D, textureId);
    }
    
    /**
     * Binds the texture to texture unit 0.
     */
    public void bind() {
        bind(0);
    }
    
    /**
     * Unbinds the texture.
     */
    public void unbind() {
        glBindTexture(GL_TEXTURE_2D, 0);
    }
    
    /**
     * Cleans up the texture.
     */
    public void cleanup() {
        glDeleteTextures(textureId);
    }
}
