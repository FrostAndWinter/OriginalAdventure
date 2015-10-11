#version 330

smooth in vec3 vertexNormal;
smooth in vec2 textureCoordinate;
smooth in vec3 cameraSpacePosition;

smooth in mat3 cameraToTangentSpaceMatrix;

layout (location = 0) out vec3 cameraSpacePositionOut;
layout (location = 1) out vec3 diffuseColourOut;
layout (location = 2) out vec3 vertexNormalOut;
layout (location = 3) out vec2 textureCoordinateOut;

uniform Material {
    vec4 ambientColour; //of which xyz are the colour and w is a 0/1 as to whether ambient self-illumination is enabled.
    vec4 diffuseColour; //r,g,b,a
    vec4 specularColour; //of which xyz are the colour and w is the specularity.
    int booleanMask;
} material;

uniform sampler2D ambientColourSampler;
uniform sampler2D diffuseColourSampler;
uniform sampler2D specularColourSampler;
uniform sampler2D specularitySampler;
uniform sampler2D normalMapSampler;

vec4 diffuseColour() {
    if ((material.booleanMask & (1 << 1)) != 0) {
        return texture(diffuseColourSampler, textureCoordinate);
    } else {
        return material.diffuseColour;
    }
}
											
void main()									
{											
	cameraSpacePositionOut = cameraSpacePosition;
	diffuseColourOut = diffuseColour().rgb;
	vertexNormalOut = vertexNormal;
	textureCoordinateOut = textureCoordinate;
}
