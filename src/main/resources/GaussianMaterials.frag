#version 330

smooth in vec3 vertexNormal;
smooth in vec3 cameraSpacePosition;

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
} material;

uniform float maxIntensity;
uniform float gamma;

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

vec3 ComputeLighting(in PerLightData lightData) {
	vec3 lightDirection;
	vec3 lightIntensity;
	if (lightData.positionInCameraSpace.w < 0.0001) {
		lightDirection = lightData.positionInCameraSpace.xyz;
		lightIntensity = lightData.lightIntensity.xyz;
	}
	else {
		float attenuation = ComputeAttenuation(cameraSpacePosition,
			lightData.positionInCameraSpace.xyz, lightData.lightIntensity.w, lightDirection);
		lightIntensity = attenuation * lightData.lightIntensity.xyz;
	}

	vec3 surfaceNormal = normalize(vertexNormal);
	float cosAngIncidence = dot(surfaceNormal, lightDirection);
	cosAngIncidence = cosAngIncidence < 0.0001 ? 0.0f : cosAngIncidence; //clamp it to 0

	vec3 viewDirection = normalize(-cameraSpacePosition);

	vec3 halfAngle = normalize(lightDirection + viewDirection);
	float angleNormalHalf = acos(dot(halfAngle, surfaceNormal));
	float exponent = angleNormalHalf / material.specularColour.w;
	exponent = -(exponent * exponent);
	float gaussianTerm = exp(exponent);

	gaussianTerm = cosAngIncidence != 0.0f ? gaussianTerm : 0.0;

	vec3 lighting = material.diffuseColour.xyz * lightIntensity * cosAngIncidence;
	lighting += material.specularColour.xyz * lightIntensity * gaussianTerm;

	return lighting;
}

void main() {
	vec3 totalLighting = material.diffuseColour.xyz * lighting.ambientIntensity.xyz;

	if (material.ambientColour.w > 0.9f) { // ~= 1
	    totalLighting += material.ambientColour.xyz;
	}

	for (int light = 0; light < lighting.numDynamicLights; light++) {
		totalLighting += ComputeLighting(lighting.lights[light]);
	}

	totalLighting = totalLighting / maxIntensity;

	vec3 gammaVector = vec3(1.f / gamma);
	outputColor = vec4(pow(totalLighting, gammaVector), material.diffuseColour.w);
}
