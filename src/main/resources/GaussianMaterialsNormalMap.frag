#version 330

smooth in vec3 vertexNormal;
smooth in vec2 textureCoordinate;
smooth in vec3 cameraSpacePosition;

smooth in mat3 cameraToTangentSpaceMatrix;

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

uniform float maxIntensity;

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

bool useNormalMap() {
	return (material.booleanMask & (1 << 4)) != 0;
}

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

vec3 ComputeLightingUsingNormalMap(in PerLightData lightData) {
	vec3 lightDirection;
	vec3 lightIntensity;

	if (lightData.positionInCameraSpace.w < 0.0001) {
		lightDirection = normalize(cameraToTangentSpaceMatrix * lightData.positionInCameraSpace.xyz);
		lightIntensity = lightData.lightIntensity.rgb;
	}
	else {
		float attenuation = ComputeAttenuation(cameraSpacePosition,
			lightData.positionInCameraSpace.xyz, lightData.lightIntensity.w, lightDirection);
		lightIntensity = attenuation * lightData.lightIntensity.rgb;
		lightDirection = normalize(cameraToTangentSpaceMatrix * lightDirection);
	}

	vec3 surfaceNormal = normalize(texture(normalMapSampler, textureCoordinate).rgb*2.0 - 1.0);
	float cosAngIncidence = dot(surfaceNormal, lightDirection);
	cosAngIncidence = cosAngIncidence < 0.0001 ? 0.0f : cosAngIncidence; //clamp it to 0

	vec3 viewDirection = normalize(cameraToTangentSpaceMatrix * -cameraSpacePosition);

	vec3 halfAngle = normalize(lightDirection + viewDirection);
	float angleNormalHalf = acos(dot(halfAngle, surfaceNormal));
	float exponent = angleNormalHalf / specularity();
	exponent = -(exponent * exponent);
	float gaussianTerm = exp(exponent);

	gaussianTerm = cosAngIncidence != 0.0f ? gaussianTerm : 0.0;

	vec3 lighting = diffuseColour().rgb * lightIntensity * cosAngIncidence;
	lighting += specularColour().rgb * lightIntensity * gaussianTerm;

	lighting = vec3(cosAngIncidence);

	return lighting;
}

vec3 ComputeLighting(in PerLightData lightData) {
	vec3 lightDirection;
	vec3 lightIntensity;
	if (lightData.positionInCameraSpace.w < 0.0001) {
		lightDirection = lightData.positionInCameraSpace.xyz;
		lightIntensity = lightData.lightIntensity.rgb;
	}
	else {
		float attenuation = ComputeAttenuation(cameraSpacePosition,
			lightData.positionInCameraSpace.xyz, lightData.lightIntensity.w, lightDirection);
		lightIntensity = attenuation * lightData.lightIntensity.rgb;
	}

	vec3 surfaceNormal = normalize(vertexNormal);
	float cosAngIncidence = dot(surfaceNormal, lightDirection);
	cosAngIncidence = cosAngIncidence < 0.0001 ? 0.0f : cosAngIncidence; //clamp it to 0

	vec3 viewDirection = normalize(-cameraSpacePosition);

	vec3 halfAngle = normalize(lightDirection + viewDirection);
	float angleNormalHalf = acos(dot(halfAngle, surfaceNormal));
	float exponent = angleNormalHalf / specularity();
	exponent = -(exponent * exponent);
	float gaussianTerm = exp(exponent);

	gaussianTerm = cosAngIncidence != 0.0f ? gaussianTerm : 0.0;

	vec3 lighting = diffuseColour().rgb * lightIntensity * cosAngIncidence;
	lighting += specularColour().rgb * lightIntensity * gaussianTerm;

	return lighting;
}

void main() {

	if (material.diffuseColour.a < 0.001f) {
	    discard;
	}

	vec3 totalLighting = diffuseColour().rgb * lighting.ambientIntensity.rgb;

	if (material.ambientColour.a > 0.9f) { // ~= 1
	    totalLighting += ambientColour().rgb;
	}

	bool useNormalMap = useNormalMap();

	for (int light = 0; light < lighting.numDynamicLights; light++) {
		totalLighting += useNormalMap ? ComputeLightingUsingNormalMap(lighting.lights[light]) : ComputeLighting(lighting.lights[light]);
	}

	totalLighting = totalLighting / maxIntensity;

	outputColor = vec4(totalLighting, material.diffuseColour.a);
}
