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
uniform mat3 normalModelToCameraMatrixUniform;

uniform Material {
   vec4 ambientColour; //of which xyz are the colour and w is a 0/1 as to whether ambient self-illumination is enabled.
   vec4 diffuseColour; //r,g,b,a
   vec4 specularColour; //of which xyz are the colour and w is the specularity.
   vec2 textureScale;
   int booleanMask;
} material;

void main() {

    vec4 cameraPos = modelToCameraMatrixUniform * position;
    cameraSpacePosition = cameraPos.xyz;
	gl_Position = cameraToClipMatrixUniform * cameraPos;
	vertexNormal = normalModelToCameraMatrixUniform * normal;
	textureCoordinate = texCoord.st * material.textureScale;
}
