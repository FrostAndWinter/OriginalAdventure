#version 330

smooth in vec3 vertexNormal;
smooth in vec2 textureCoordinate;
smooth in vec3 cameraSpacePosition;

smooth in mat3 tangentToCameraSpaceMatrix;

layout (location = 0) out vec3 vertexNormalOut;
layout (location = 1) out vec3 diffuseColourOut;
layout (location = 2) out vec4 specularColourOut;
layout (location = 3) out vec3 ambientColourOut;


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

bool useNormalMap() {
    return (material.booleanMask & (1 << 4)) != 0;
}

vec4 diffuseColour() {
    if ((material.booleanMask & (1 << 1)) != 0) {
        return texture(diffuseColourSampler, textureCoordinate);
    } else {
        return material.diffuseColour;
    }
}

vec4 ambientColour() {

    if ((material.booleanMask & (1)) != 0) {
        return texture(ambientColourSampler, textureCoordinate);
    } else {
        return material.ambientColour;
    }
}

vec4 specularColour() {
    if ((material.booleanMask & (1 << 2)) != 0) {
        return texture(specularColourSampler, textureCoordinate);
    } else {
        return material.specularColour;
    }
}

float specularity() {
    if ((material.booleanMask & (1 << 3)) != 0) {
        return 1.f/min(texture(specularitySampler, textureCoordinate).r, 1.f);
    } else {
        return material.specularColour.a;
    }
}
											
void main()									
{
    vec4 diffuseColour = diffuseColour();
    vec4 ambientColour = ambientColour();
    if (diffuseColour.a < 0.001f) {
       discard;
    }

    vec3 cameraSpaceNormal;
    if (useNormalMap()) {
        cameraSpaceNormal = normalize(tangentToCameraSpaceMatrix * (texture(normalMapSampler, textureCoordinate).rgb*2.0 - 1.0));
    } else {
        cameraSpaceNormal = normalize(vertexNormal);
    }

    vertexNormalOut = cameraSpaceNormal + 1;

    diffuseColourOut = diffuseColour.rgb;

    if (ambientColour.a > 0.9f) { // ~= 1
         ambientColourOut = ambientColour.rgb;
    }

    specularColourOut = vec4(specularColour().rgb, specularity());
}
