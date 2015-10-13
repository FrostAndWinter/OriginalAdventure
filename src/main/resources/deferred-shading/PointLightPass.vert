#version 330

layout(location = 0) in vec4 position;

uniform mat4 modelToCameraMatrixUniform;
uniform mat4 cameraToClipMatrixUniform;
uniform vec2 screenSizeUniform;

//Half the size of the near plane {y * aspect, tan(fovy/2.0) }
uniform vec2 halfSizeNearPlane;

out vec3 eyeDirection;

void main() {

    vec4 cameraPos = modelToCameraMatrixUniform * position;
	vec4 clipPos = cameraToClipMatrixUniform * cameraPos;
    gl_Position = clipPos;

    vec2 clipPosAfterDivide = clipPos.xy / clipPos.w; //get the x and y position of this vertex.
    vec2 textureCoordinate = (clipPosAfterDivide + 1)/2.f; //get the x and y position of this vertex.

	eyeDirection = vec3((2.0 * halfSizeNearPlane * textureCoordinate) - halfSizeNearPlane, -1.0);
}
