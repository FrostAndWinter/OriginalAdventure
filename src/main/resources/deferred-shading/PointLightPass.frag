#version 330

out vec4 outputColor;

layout(std140) uniform;

uniform PointLight {
    vec4 attenuation; //where [0] is constant, [1] is linear, and [2] is the quadratic coefficient
    vec4 positionInCameraSpace;
    vec4 intensity;
} light;


uniform mat4 cameraToClipMatrixUniform;
uniform vec2 depthRangeUniform;
uniform vec2 screenSizeUniform;

//Half the size of the near plane {y * aspect, tan(fovy/2.0) } // { 0.7698 , 0.577350 } for fov = pi/3 and aspect = 4:3
uniform vec2 halfSizeNearPlaneUniform;

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

float ComputeAttenuation(in vec3 objectPosition,
    in vec3 lightPosition,
    out vec3 lightDirection) {

        vec3 vectorToLight = lightPosition - objectPosition;
        float lightDistanceSqr = dot(vectorToLight, vectorToLight);
        float inverseLightDistance = inversesqrt(lightDistanceSqr);
        lightDirection = vectorToLight * inverseLightDistance;

        float invLightDistanceSq = 1.f/lightDistanceSqr;

        return invLightDistanceSq / (light.attenuation[0] * invLightDistanceSq + light.attenuation[1] * inverseLightDistance + light.attenuation[2]);
}

float ComputeAngleNormalHalf(in vec3 cameraSpacePosition, in vec3 surfaceNormal, out float cosAngIncidence, out vec3 lightIntensity) {

    vec3 lightDirection;
    float attenuation = ComputeAttenuation(cameraSpacePosition,
            light.positionInCameraSpace.xyz, lightDirection);
    lightIntensity = attenuation * light.intensity.rgb;

    vec3 viewDirection = normalize(-cameraSpacePosition);

    float cosAngIncidenceTemp = dot(surfaceNormal, lightDirection);
    cosAngIncidence = cosAngIncidenceTemp < 0.0001 ? 0.0f : cosAngIncidenceTemp; //clamp it to 0
    vec3 halfAngle = normalize(lightDirection + viewDirection);
    float angleNormalHalf = acos(dot(halfAngle, surfaceNormal));
    return angleNormalHalf;
}

vec3 ComputeLighting(in vec3 cameraSpacePosition, in vec3 surfaceNormal, in vec3 diffuse, in vec4 specular) {

    vec3 lightIntensity;
    float cosAngIncidence;

    float angleNormalHalf = ComputeAngleNormalHalf(cameraSpacePosition, surfaceNormal, cosAngIncidence, lightIntensity);

    float exponent = angleNormalHalf / specular.a;
    exponent = -(exponent * exponent);
    float gaussianTerm = exp(exponent);

    gaussianTerm = cosAngIncidence != 0.0f ? gaussianTerm : 0.0;

    vec3 lighting = diffuse * lightIntensity * cosAngIncidence;
    lighting += specular.rgb * lightIntensity * gaussianTerm;

    return lighting;
}

vec2 CalcTexCoord() {
   return gl_FragCoord.xy / screenSizeUniform;
}

void main() {

    vec2 textureCoordinate = CalcTexCoord();

    vec3 cameraDirection = vec3((2.0 * halfSizeNearPlaneUniform * textureCoordinate) - halfSizeNearPlaneUniform, -1.0);
    vec3 cameraSpacePosition = CalcCameraSpacePositionFromWindow(texture(depthSampler, textureCoordinate).r, cameraDirection);

	vec3 diffuseColour = texture(diffuseColourSampler, textureCoordinate).rgb;
	vec4 specularColour = texture(specularColourSampler, textureCoordinate);

	vec3 surfaceNormal = texture(cameraSpaceNormalSampler, textureCoordinate).xyz - 1;

    vec3 totalLighting = ComputeLighting(cameraSpacePosition, surfaceNormal, diffuseColour, specularColour);

    outputColor = vec4(totalLighting, 1.f);

}