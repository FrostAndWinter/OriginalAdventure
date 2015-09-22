#version 330

layout(location = 0) in vec4 position;
layout(location = 1) in vec3 textureCoordinate;
layout(location = 2) in vec3 normal;

smooth out vec4 diffuseColour;
smooth out vec3 vertexNormal;
smooth out vec3 cameraSpacePosition;

uniform mat4 modelToCameraMatrixUniform;
uniform mat4 cameraToClipMatrixUniform;
uniform mat4 normalModelToCameraMatrixUniform;
uniform vec4 colour;

void main() {

    vec4 cameraPos = modelToCameraMatrixUniform * position;
    cameraSpacePosition = cameraPos.xyz;
	gl_Position = cameraToClipMatrixUniform * cameraPos;
	diffuseColour = colour;
	vertexNormal = normal;
}
