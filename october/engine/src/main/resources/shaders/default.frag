#version 460 core

// Inputs from vertex shader
in vec2 vTexCoord;

// Output color
out vec4 FragColor;

// Texture sampler
uniform sampler2D uTextureSampler;

void main()
{
    FragColor = texture(uTextureSampler, vTexCoord);
}