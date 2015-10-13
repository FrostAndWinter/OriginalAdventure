#version 330

out vec4 outputColor;

layout(std140) uniform;

struct PerLightData {
    vec4 positionInCameraSpace;
    vec4 lightIntensity;
};

const int MaxLights = 32;

uniform Light {
    vec4 ambientIntensity;
    int numDynamicLights;
    int padding1;
    float lightAttenuationFactor;
    float padding2;
    PerLightData lights[MaxLights];
} lighting;

in vec2 textureCoordinate;

uniform mat4 cameraToClipMatrixUniform;
uniform vec2 depthRangeUniform;
uniform vec2 screenSizeUniform;

//Half the size of the near plane {y * aspect, tan(fovy/2.0) } // { 0.7698 , 0.577350 } for fov = pi/3 and aspect = 4:3
uniform vec2 halfSizeNearPlaneUniform;

uniform sampler2D ambientColourSampler;
uniform sampler2D diffuseColourSampler;
uniform sampler2D specularColourSampler;

uniform sampler2D cameraSpaceNormalSampler;

uniform sampler2D depthSampler;

vec3 CalcCameraSpacePositionFromWindow(in float windowZ, in vec3 eyeDirection) {
  float ndcZ = (2.0 * windowZ - depthRangeUniform.x - depthRangeUniform.y) /
    (depthRangeUniform.y - depthRangeUniform.x);
  float eyeZ = -cameraToClipMatrixUniform[3][2] / ((cameraToClipMatrixUniform[2][3] * ndcZ) - cameraToClipMatrixUniform[2][2]);
  return eyeDirection * eyeZ;
}

float ComputeAngleNormalHalf(in PerLightData lightData, in vec3 cameraSpacePosition, in vec3 surfaceNormal, out float cosAngIncidence, out vec3 lightIntensity) {
    vec3 lightDirection = lightData.positionInCameraSpace.xyz;
    lightIntensity = lightData.lightIntensity.rgb;

    vec3 viewDirection = normalize(-cameraSpacePosition);

    float cosAngIncidenceTemp = dot(surfaceNormal, lightDirection);
    cosAngIncidence = cosAngIncidenceTemp < 0.0001 ? 0.0f : cosAngIncidenceTemp; //clamp it to 0
    vec3 halfAngle = normalize(lightDirection + viewDirection);
    float angleNormalHalf = acos(dot(halfAngle, surfaceNormal));
    return angleNormalHalf;
}

vec3 ComputeLighting(in PerLightData lightData, in vec3 cameraSpacePosition, in vec3 surfaceNormal, in vec3 diffuse, in vec4 specular) {
    vec3 lightIntensity;
    float cosAngIncidence;

    float angleNormalHalf = ComputeAngleNormalHalf(lightData, cameraSpacePosition, surfaceNormal, cosAngIncidence, lightIntensity);

    float exponent = angleNormalHalf / specular.a;
    exponent = -(exponent * exponent);
    float gaussianTerm = exp(exponent);

    gaussianTerm = cosAngIncidence != 0.0f ? gaussianTerm : 0.0;

    vec3 lighting = diffuse * lightIntensity * cosAngIncidence;
    lighting += specular.rgb * lightIntensity * gaussianTerm;

    return lighting;
}

void main() {

    vec3 cameraDirection = vec3((2.0 * halfSizeNearPlaneUniform * textureCoordinate) - halfSizeNearPlaneUniform, -1.0);
    vec3 cameraSpacePosition = CalcCameraSpacePositionFromWindow(texture(depthSampler, textureCoordinate).r, cameraDirection);

	vec3 diffuseColour = texture(diffuseColourSampler, textureCoordinate).rgb;
	vec4 specularColour = texture(specularColourSampler, textureCoordinate);
	vec4 ambientColour = texture(ambientColourSampler, textureCoordinate);

	vec3 surfaceNormal = texture(cameraSpaceNormalSampler, textureCoordinate).xyz - 1;

    vec3 totalLighting = diffuseColour * lighting.ambientIntensity.rgb;

    for (int light = 0; light < lighting.numDynamicLights; light++) {
        totalLighting += ComputeLighting(lighting.lights[light], cameraSpacePosition, surfaceNormal, diffuseColour, specularColour);
    }

    outputColor = vec4(totalLighting, 1.f);

}