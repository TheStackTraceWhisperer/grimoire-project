#version 460 core

// Inputs from vertex shader
in vec2 vTexCoord;

// Output color
out vec4 fColor;

// Uniforms
uniform sampler2D uTextureSampler;
uniform vec4 uColor; // tint color (rgba)

void main()
{
    // Sample the texture at the given texture coordinates and apply tint
    fColor = texture(uTextureSampler, vTexCoord) * uColor;
}