#version 330

layout(location = 0) in vec4 position;

uniform mat4 modelToCameraMatrixUniform;
uniform mat4 cameraToClipMatrixUniform;

void main() {

    vec4 cameraPos = modelToCameraMatrixUniform * position;
	gl_Position = cameraToClipMatrixUniform * cameraPos;
}
