#version 450 core

in vec2 vTexCoord;
in vec4 vColor;

out vec4 fragColor;

uniform sampler2D textureSampler;
uniform vec4 color;
uniform int useTexture;

void main() {
    vec4 texColor = texture(textureSampler, vTexCoord);
    
    if (useTexture == 1) {
        fragColor = texColor * color * vColor;
    } else {
        fragColor = color * vColor;
    }
    
    // Discard fully transparent pixels
    if (fragColor.a < 0.01) {
        discard;
    }
}
