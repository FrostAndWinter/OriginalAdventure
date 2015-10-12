#version 330

layout(location = 0) in vec4 position;
layout(location = 1) in vec3 texCoord;
layout(location = 2) in vec3 normal;
layout(location = 3) in vec4 tangent;

smooth out vec3 vertexNormal;
smooth out vec3 cameraSpacePosition;
smooth out vec2 textureCoordinate;

smooth out mat3 tangentToCameraSpaceMatrix;

uniform mat4 modelToCameraMatrixUniform;
uniform mat4 cameraToClipMatrixUniform;
uniform mat3 normalModelToCameraMatrixUniform;
uniform mat3 nodeToCamera3x3MatrixUniform;

uniform vec2 textureRepeatUniform;

void main() {

    vec4 cameraPos = modelToCameraMatrixUniform * position;
    cameraSpacePosition = cameraPos.xyz;
	gl_Position = cameraToClipMatrixUniform * cameraPos;
	vertexNormal = normalModelToCameraMatrixUniform * normal;
	textureCoordinate = texCoord.st * textureRepeatUniform;

	vec3 mBPrime = tangent.w * (cross(normal, tangent.xyz));

	tangentToCameraSpaceMatrix = mat3(
	          normalize(nodeToCamera3x3MatrixUniform * tangent.xyz),
	          normalize(nodeToCamera3x3MatrixUniform * mBPrime),
	          normalize(nodeToCamera3x3MatrixUniform * normal)
	);
}
