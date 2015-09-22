#version 330

in vec4 diffuseColour;
in vec3 vertexNormal;
in vec3 cameraSpacePosition;

out vec4 outputColor;

uniform vec3 cameraSpaceLightPosition;

uniform vec4 lightIntensity;
uniform vec4 ambientIntensity;

void main()
{
	vec3 lightDir = normalize(cameraSpaceLightPosition - cameraSpacePosition);

	float cosAngIncidence = dot(normalize(vertexNormal), lightDir);
	cosAngIncidence = clamp(cosAngIncidence, 0, 1);

	if (dot(vertexNormal, vertexNormal) == 0) {
	    outputColor = diffuseColour;
	} else {
	    outputColor = (diffuseColour * lightIntensity * cosAngIncidence) +
        		(diffuseColour * ambientIntensity);
	}
}
