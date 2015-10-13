#version 330

layout (location = 0) in vec3 position;

out vec2 textureCoordinate;

void main()
{
    textureCoordinate = (position.xy + 1.f)/2.f;
    gl_Position = vec4(position, 1.f);
}
