#version 460 core

// Per-vertex attributes
layout (location = 0) in vec3 aPos;
layout (location = 1) in vec2 aTexCoord;

// Per-instance attributes (transformation matrix)
layout (location = 2) in vec4 aInstanceMatrix0;
layout (location = 3) in vec4 aInstanceMatrix1;
layout (location = 4) in vec4 aInstanceMatrix2;
layout (location = 5) in vec4 aInstanceMatrix3;

// Outputs to fragment shader
out vec2 vTexCoord;

// Scene uniforms
uniform mat4 uProjection;
uniform mat4 uView;

void main()
{
    // Reconstruct instance transformation matrix from vec4 attributes
    mat4 instanceMatrix = mat4(
        aInstanceMatrix0,
        aInstanceMatrix1,
        aInstanceMatrix2,
        aInstanceMatrix3
    );
    
    // Transform vertex position using instance matrix
    gl_Position = uProjection * uView * instanceMatrix * vec4(aPos, 1.0);
    
    // Pass through texture coordinates unchanged
    vTexCoord = aTexCoord;
}