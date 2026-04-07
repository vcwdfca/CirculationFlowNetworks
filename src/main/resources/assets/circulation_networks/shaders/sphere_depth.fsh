#version 150

uniform sampler2D u_DepthTexture;
uniform vec2 u_ScreenSize;
uniform float u_Near;
uniform float u_Far;
uniform vec4 u_SphereColor;
uniform vec4 u_IntersectionColor;
uniform float u_IntersectionWidth;

out vec4 fragColor;

float linearizeDepth(float d) {
    float z_ndc = d * 2.0 - 1.0;
    return (2.0 * u_Near * u_Far) / (u_Far + u_Near - z_ndc * (u_Far - u_Near));
}

void main() {
    vec2 screenUV = gl_FragCoord.xy / u_ScreenSize;
    float sceneDepth = texture(u_DepthTexture, screenUV).r;

    float sceneLinear = linearizeDepth(sceneDepth);
    float fragLinear = linearizeDepth(gl_FragCoord.z);

    float diff = abs(sceneLinear - fragLinear);

    float halfWidth = u_IntersectionWidth;
    float intersection = 1.0 - smoothstep(halfWidth, u_IntersectionWidth, diff);

    fragColor = mix(u_SphereColor, u_IntersectionColor, intersection);
}
