#version 450 core

in vec2 vTexCoord;
in vec4 vColor;

out vec4 fragColor;

uniform sampler2D textureSampler;
uniform int isText;

void main() {
    if (isText == 1) {
        // For text rendering, use red channel as alpha (SDF font)
        float alpha = texture(textureSampler, vTexCoord).r;
        fragColor = vec4(vColor.rgb, vColor.a * alpha);
    } else {
        // Standard texture or color rendering
        vec4 texColor = texture(textureSampler, vTexCoord);
        fragColor = texColor * vColor;
    }
    
    if (fragColor.a < 0.01) {
        discard;
    }
}
