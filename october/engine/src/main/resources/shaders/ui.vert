#version 460 core

// Per-vertex attributes
layout (location = 0) in vec3 aPos;
layout (location = 1) in vec2 aTexCoord;

// Outputs to fragment shader
out vec2 vTexCoord;

// Uniforms
uniform mat4 uProjection;
uniform mat4 uView;
uniform mat4 uModel;

void main()
{
    // Transform vertex position
    gl_Position = uProjection * uView * uModel * vec4(aPos, 1.0);
    
    // Pass through texture coordinates
    vTexCoord = aTexCoord;
}