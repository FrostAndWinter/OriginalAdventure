#version 330

out vec4 outputColor;

uniform vec3 colourUniform;

void main() {
    outputColor = vec4(colourUniform, 1.f);
}
