package com.grimoire.clientv2.render.mesh;

import lombok.Getter;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL45.*;

/**
 * Mesh class for managing OpenGL vertex data.
 * Handles VAO, VBO, and EBO creation and management.
 */
public class Mesh {
    
    @Getter
    private final int vaoId;
    
    private final int posVboId;
    private final int texCoordVboId;
    private final int colorVboId;
    private final int eboId;
    
    @Getter
    private final int vertexCount;
    
    /**
     * Creates a mesh with positions, texture coordinates, colors, and indices.
     *
     * @param positions  Vertex positions (3 floats per vertex: x, y, z)
     * @param texCoords  Texture coordinates (2 floats per vertex: u, v)
     * @param colors     Vertex colors (4 floats per vertex: r, g, b, a)
     * @param indices    Indices for indexed rendering
     */
    public Mesh(float[] positions, float[] texCoords, float[] colors, int[] indices) {
        FloatBuffer posBuffer = null;
        FloatBuffer texCoordBuffer = null;
        FloatBuffer colorBuffer = null;
        IntBuffer indicesBuffer = null;
        
        try {
            vertexCount = indices.length;
            
            // Create VAO
            vaoId = glCreateVertexArrays();
            glBindVertexArray(vaoId);
            
            // Position VBO
            posVboId = glGenBuffers();
            posBuffer = MemoryUtil.memAllocFloat(positions.length);
            posBuffer.put(positions).flip();
            glBindBuffer(GL_ARRAY_BUFFER, posVboId);
            glBufferData(GL_ARRAY_BUFFER, posBuffer, GL_STATIC_DRAW);
            glEnableVertexAttribArray(0);
            glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
            
            // Texture coordinate VBO
            texCoordVboId = glGenBuffers();
            texCoordBuffer = MemoryUtil.memAllocFloat(texCoords.length);
            texCoordBuffer.put(texCoords).flip();
            glBindBuffer(GL_ARRAY_BUFFER, texCoordVboId);
            glBufferData(GL_ARRAY_BUFFER, texCoordBuffer, GL_STATIC_DRAW);
            glEnableVertexAttribArray(1);
            glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
            
            // Color VBO
            colorVboId = glGenBuffers();
            colorBuffer = MemoryUtil.memAllocFloat(colors.length);
            colorBuffer.put(colors).flip();
            glBindBuffer(GL_ARRAY_BUFFER, colorVboId);
            glBufferData(GL_ARRAY_BUFFER, colorBuffer, GL_STATIC_DRAW);
            glEnableVertexAttribArray(2);
            glVertexAttribPointer(2, 4, GL_FLOAT, false, 0, 0);
            
            // Index buffer (EBO)
            eboId = glGenBuffers();
            indicesBuffer = MemoryUtil.memAllocInt(indices.length);
            indicesBuffer.put(indices).flip();
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, eboId);
            glBufferData(GL_ELEMENT_ARRAY_BUFFER, indicesBuffer, GL_STATIC_DRAW);
            
            // Unbind
            glBindBuffer(GL_ARRAY_BUFFER, 0);
            glBindVertexArray(0);
            
        } finally {
            if (posBuffer != null) MemoryUtil.memFree(posBuffer);
            if (texCoordBuffer != null) MemoryUtil.memFree(texCoordBuffer);
            if (colorBuffer != null) MemoryUtil.memFree(colorBuffer);
            if (indicesBuffer != null) MemoryUtil.memFree(indicesBuffer);
        }
    }
    
    /**
     * Creates a simple mesh with positions and indices only.
     *
     * @param positions Vertex positions (3 floats per vertex)
     * @param indices   Indices for indexed rendering
     */
    public Mesh(float[] positions, int[] indices) {
        this(positions, 
             createDefaultTexCoords(positions.length / 3),
             createDefaultColors(positions.length / 3),
             indices);
    }
    
    private static float[] createDefaultTexCoords(int vertexCount) {
        float[] texCoords = new float[vertexCount * 2];
        for (int i = 0; i < texCoords.length; i++) {
            texCoords[i] = 0.0f;
        }
        return texCoords;
    }
    
    private static float[] createDefaultColors(int vertexCount) {
        float[] colors = new float[vertexCount * 4];
        for (int i = 0; i < vertexCount; i++) {
            colors[i * 4] = 1.0f;     // R
            colors[i * 4 + 1] = 1.0f; // G
            colors[i * 4 + 2] = 1.0f; // B
            colors[i * 4 + 3] = 1.0f; // A
        }
        return colors;
    }
    
    /**
     * Renders the mesh.
     */
    public void render() {
        glBindVertexArray(vaoId);
        glDrawElements(GL_TRIANGLES, vertexCount, GL_UNSIGNED_INT, 0);
        glBindVertexArray(0);
    }
    
    /**
     * Cleans up OpenGL resources.
     */
    public void cleanup() {
        glDisableVertexAttribArray(0);
        glDisableVertexAttribArray(1);
        glDisableVertexAttribArray(2);
        
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glDeleteBuffers(posVboId);
        glDeleteBuffers(texCoordVboId);
        glDeleteBuffers(colorVboId);
        
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
        glDeleteBuffers(eboId);
        
        glBindVertexArray(0);
        glDeleteVertexArrays(vaoId);
    }
    
    /**
     * Creates a quad mesh (useful for 2D rendering and UI).
     *
     * @param width  Width of the quad
     * @param height Height of the quad
     * @return A new mesh representing a quad
     */
    public static Mesh createQuad(float width, float height) {
        float halfW = width / 2.0f;
        float halfH = height / 2.0f;
        
        float[] positions = {
            -halfW,  halfH, 0.0f,  // Top-left
             halfW,  halfH, 0.0f,  // Top-right
             halfW, -halfH, 0.0f,  // Bottom-right
            -halfW, -halfH, 0.0f   // Bottom-left
        };
        
        float[] texCoords = {
            0.0f, 0.0f,  // Top-left
            1.0f, 0.0f,  // Top-right
            1.0f, 1.0f,  // Bottom-right
            0.0f, 1.0f   // Bottom-left
        };
        
        float[] colors = {
            1.0f, 1.0f, 1.0f, 1.0f,
            1.0f, 1.0f, 1.0f, 1.0f,
            1.0f, 1.0f, 1.0f, 1.0f,
            1.0f, 1.0f, 1.0f, 1.0f
        };
        
        int[] indices = { 0, 1, 2, 2, 3, 0 };
        
        return new Mesh(positions, texCoords, colors, indices);
    }
    
    /**
     * Creates a unit quad at origin (0-1 coordinates).
     */
    public static Mesh createUnitQuad() {
        float[] positions = {
            0.0f, 1.0f, 0.0f,  // Top-left
            1.0f, 1.0f, 0.0f,  // Top-right
            1.0f, 0.0f, 0.0f,  // Bottom-right
            0.0f, 0.0f, 0.0f   // Bottom-left
        };
        
        float[] texCoords = {
            0.0f, 0.0f,
            1.0f, 0.0f,
            1.0f, 1.0f,
            0.0f, 1.0f
        };
        
        float[] colors = {
            1.0f, 1.0f, 1.0f, 1.0f,
            1.0f, 1.0f, 1.0f, 1.0f,
            1.0f, 1.0f, 1.0f, 1.0f,
            1.0f, 1.0f, 1.0f, 1.0f
        };
        
        int[] indices = { 0, 1, 2, 2, 3, 0 };
        
        return new Mesh(positions, texCoords, colors, indices);
    }
}
