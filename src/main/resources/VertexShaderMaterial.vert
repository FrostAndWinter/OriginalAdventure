#version 330

layout(location = 0) in vec4 position;
layout(location = 1) in vec3 texCoord;
layout(location = 2) in vec3 normal;

smooth out vec3 vertexNormal;
smooth out vec3 cameraSpacePosition;
smooth out vec2 textureCoordinate;

uniform mat4 modelToCameraMatrixUniform;
uniform mat4 cameraToClipMatrixUniform;
uniform mat3 normalModelToCameraMatrixUniform;

void main() {

    vec4 cameraPos = modelToCameraMatrixUniform * position;
    cameraSpacePosition = cameraPos.xyz;
	gl_Position = cameraToClipMatrixUniform * cameraPos;
	vertexNormal = normalModelToCameraMatrixUniform * normal;
	textureCoordinate = texCoord.xy;
}
