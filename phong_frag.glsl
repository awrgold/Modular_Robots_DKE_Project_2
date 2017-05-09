#version 330 core

in vec3 frag_normal;

uniform vec4 colour;

out vec4 fragColor;

void main() {
    float aLight = 0.2;
    vec3 lightDir = vec3(-0.5, -0.4, -0.3);
    lightDir = normalize(lightDir);
    fragColor = vec4(vec3(1.0, 1.0, 1.0) * clamp(-dot(frag_normal, lightDir), 0.0, 1.0)+aLight, 1.0) * colour;
}
