#version 330

layout(location = 0) in vec4 position;
layout(location = 1) in vec4 color;

smooth out vec4 interpColor;

uniform mat4 modelToClipMatrixUniform;

void main() {
	gl_Position = modelToClipMatrixUniform * position;
	interpColor = color;
}
