package com.grimoire.client.render.shader;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryStack;

import java.io.IOException;
import java.io.InputStream;
import java.nio.FloatBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL45.*;

/**
 * OpenGL shader program wrapper.
 * Handles shader compilation, linking, and uniform management.
 */
@Slf4j
public class ShaderProgram {
    
    @Getter
    private final int programId;
    
    private int vertexShaderId;
    private int fragmentShaderId;
    
    private final Map<String, Integer> uniformLocations;
    
    /**
     * Creates a new shader program.
     */
    public ShaderProgram() {
        programId = glCreateProgram();
        if (programId == 0) {
            throw new RuntimeException("Could not create shader program");
        }
        uniformLocations = new HashMap<>();
    }
    
    /**
     * Creates a vertex shader from source.
     */
    public void createVertexShader(String shaderCode) {
        vertexShaderId = createShader(shaderCode, GL_VERTEX_SHADER);
    }
    
    /**
     * Creates a fragment shader from source.
     */
    public void createFragmentShader(String shaderCode) {
        fragmentShaderId = createShader(shaderCode, GL_FRAGMENT_SHADER);
    }
    
    private int createShader(String shaderCode, int shaderType) {
        int shaderId = glCreateShader(shaderType);
        if (shaderId == 0) {
            throw new RuntimeException("Error creating shader. Type: " + shaderType);
        }
        
        glShaderSource(shaderId, shaderCode);
        glCompileShader(shaderId);
        
        if (glGetShaderi(shaderId, GL_COMPILE_STATUS) == 0) {
            String error = glGetShaderInfoLog(shaderId, 1024);
            throw new RuntimeException("Error compiling shader: " + error);
        }
        
        glAttachShader(programId, shaderId);
        
        return shaderId;
    }
    
    /**
     * Links the shader program.
     */
    public void link() {
        glLinkProgram(programId);
        
        if (glGetProgrami(programId, GL_LINK_STATUS) == 0) {
            String error = glGetProgramInfoLog(programId, 1024);
            throw new RuntimeException("Error linking shader program: " + error);
        }
        
        // Detach shaders after linking
        if (vertexShaderId != 0) {
            glDetachShader(programId, vertexShaderId);
        }
        if (fragmentShaderId != 0) {
            glDetachShader(programId, fragmentShaderId);
        }
        
        // Validate program
        glValidateProgram(programId);
        if (glGetProgrami(programId, GL_VALIDATE_STATUS) == 0) {
            log.warn("Warning validating shader program: {}", glGetProgramInfoLog(programId, 1024));
        }
    }
    
    /**
     * Creates a uniform and caches its location.
     */
    public void createUniform(String uniformName) {
        int uniformLocation = glGetUniformLocation(programId, uniformName);
        if (uniformLocation < 0) {
            throw new RuntimeException("Could not find uniform: " + uniformName);
        }
        uniformLocations.put(uniformName, uniformLocation);
    }
    
    /**
     * Creates a uniform without throwing if not found (for optional uniforms).
     */
    public void createUniformOptional(String uniformName) {
        int uniformLocation = glGetUniformLocation(programId, uniformName);
        if (uniformLocation >= 0) {
            uniformLocations.put(uniformName, uniformLocation);
        }
    }
    
    /**
     * Sets a Matrix4f uniform.
     */
    public void setUniform(String uniformName, Matrix4f value) {
        Integer location = uniformLocations.get(uniformName);
        if (location == null) return;
        
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer buffer = stack.mallocFloat(16);
            value.get(buffer);
            glUniformMatrix4fv(location, false, buffer);
        }
    }
    
    /**
     * Sets an integer uniform.
     */
    public void setUniform(String uniformName, int value) {
        Integer location = uniformLocations.get(uniformName);
        if (location == null) return;
        glUniform1i(location, value);
    }
    
    /**
     * Sets a float uniform.
     */
    public void setUniform(String uniformName, float value) {
        Integer location = uniformLocations.get(uniformName);
        if (location == null) return;
        glUniform1f(location, value);
    }
    
    /**
     * Sets a Vector2f uniform.
     */
    public void setUniform(String uniformName, Vector2f value) {
        Integer location = uniformLocations.get(uniformName);
        if (location == null) return;
        glUniform2f(location, value.x, value.y);
    }
    
    /**
     * Sets a Vector3f uniform.
     */
    public void setUniform(String uniformName, Vector3f value) {
        Integer location = uniformLocations.get(uniformName);
        if (location == null) return;
        glUniform3f(location, value.x, value.y, value.z);
    }
    
    /**
     * Sets a Vector4f uniform.
     */
    public void setUniform(String uniformName, Vector4f value) {
        Integer location = uniformLocations.get(uniformName);
        if (location == null) return;
        glUniform4f(location, value.x, value.y, value.z, value.w);
    }
    
    /**
     * Binds this shader program for use.
     */
    public void bind() {
        glUseProgram(programId);
    }
    
    /**
     * Unbinds the shader program.
     */
    public void unbind() {
        glUseProgram(0);
    }
    
    /**
     * Cleans up the shader program.
     */
    public void cleanup() {
        unbind();
        if (programId != 0) {
            glDeleteProgram(programId);
        }
    }
    
    /**
     * Loads shader source from a resource file.
     */
    public static String loadResource(String resourcePath) throws IOException {
        try (InputStream is = ShaderProgram.class.getResourceAsStream(resourcePath)) {
            if (is == null) {
                throw new IOException("Resource not found: " + resourcePath);
            }
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
