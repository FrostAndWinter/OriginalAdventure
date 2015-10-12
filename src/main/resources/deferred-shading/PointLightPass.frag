#version 330

out vec4 outputColor;

layout(std140) uniform;

struct PerLightData {
    vec4 positionInCameraSpace;
    vec4 lightIntensity;
};

uniform Light {
    vec4 ambientIntensity;
    int numDynamicLights;
    int padding1;
    float lightAttenuationFactor;
    float padding2;
    PerLightData light;
} lighting;

uniform sampler2D diffuseColourSampler;
uniform sampler2D specularColourSampler;

uniform sampler2D cameraSpacePositionSampler;
uniform sampler2D cameraSpaceNormalSampler;

uniform float maxIntensity;
uniform vec2 screenSize;

float ComputeAttenuation(in vec3 objectPosition,
    in vec3 lightPosition,
    in float falloffType,
    out vec3 lightDirection) {

        vec3 vectorToLight = lightPosition - objectPosition;
        float lightDistanceSqr = dot(vectorToLight, vectorToLight);
        float inverseLightDistance = inversesqrt(lightDistanceSqr);
        lightDirection = vectorToLight * inverseLightDistance;

        if (falloffType < 0.0001) { //LightFalloff.None, ~= 0
            return 1.f;
        } else if (falloffType < 1.2f) { //LightFalloff.Linear, ~= 1
            return inverseLightDistance / (inverseLightDistance + lighting.lightAttenuationFactor);
        } else { //LightFalloff.Quadratic
            return (1 / (1.f + lighting.lightAttenuationFactor * lightDistanceSqr));
        }
}

float ComputeAngleNormalHalf(in PerLightData lightData, in vec3 cameraSpacePosition, in vec3 surfaceNormal, out float cosAngIncidence, out vec3 lightIntensity) {

    vec3 lightDirection;
    float attenuation = ComputeAttenuation(cameraSpacePosition,
            lightData.positionInCameraSpace.xyz, lightData.lightIntensity.w, lightDirection);
    lightIntensity = attenuation * lightData.lightIntensity.rgb;

    vec3 viewDirection = normalize(-cameraSpacePosition);

    float cosAngIncidenceTemp = dot(surfaceNormal, lightDirection);
    cosAngIncidence = cosAngIncidenceTemp < 0.0001 ? 0.0f : cosAngIncidenceTemp; //clamp it to 0
    vec3 halfAngle = normalize(lightDirection + viewDirection);
    float angleNormalHalf = acos(dot(halfAngle, surfaceNormal));
    return angleNormalHalf;
}

vec3 ComputeLighting(in PerLightData lightData, in vec3 cameraSpacePosition, in vec3 surfaceNormal, in vec4 diffuse, in vec4 specular) {
    vec3 lightIntensity;
    float cosAngIncidence;

    float angleNormalHalf = ComputeAngleNormalHalf(lightData, cameraSpacePosition, surfaceNormal, cosAngIncidence, lightIntensity);

    float exponent = angleNormalHalf / specular.a;
    exponent = -(exponent * exponent);
    float gaussianTerm = exp(exponent);

    gaussianTerm = cosAngIncidence != 0.0f ? gaussianTerm : 0.0;

    vec3 lighting = diffuse.rgb * lightIntensity * cosAngIncidence;
    lighting = specular.rgb * lightIntensity * gaussianTerm;

    return lighting;
}


vec2 CalcTexCoord() {
   return gl_FragCoord.xy / screenSize;
}

void main() {

    vec2 textureCoordinate = CalcTexCoord();
	vec3 cameraSpacePosition = texture(cameraSpacePositionSampler, textureCoordinate).xyz;

	vec4 diffuseColour = texture(diffuseColourSampler, textureCoordinate);
	vec4 specularColour = texture(specularColourSampler, textureCoordinate);

	vec3 surfaceNormal = texture(cameraSpaceNormalSampler, textureCoordinate).xyz;

    if (diffuseColour.a < 0.001f) {
        discard;
    }

    vec3 totalLighting = ComputeLighting(lighting.light, cameraSpacePosition, surfaceNormal, diffuseColour, specularColour);

    totalLighting = totalLighting / maxIntensity;

    outputColor = vec4(0.f, 0.f, 1.f, 1.f);//vec4(totalLighting, diffuseColour.a);

}