package com.flux.core.designsystem.component

/**
 * AGSL source of the voice lava: three soft blobs of the theme colors that drift with [time]
 * and grow with the voice [level]. Coordinates are normalized to the shortest side so the
 * blobs stay round on round, square and rectangular screens.
 */
internal const val LAVA_SHADER = """
uniform float2 size;
uniform float time;
uniform float level;
layout(color) uniform half4 c1;
layout(color) uniform half4 c2;
layout(color) uniform half4 c3;
layout(color) uniform half4 bg;

float blob(float2 uv, float2 center, float radius) {
    return smoothstep(radius, 0.0, distance(uv, center));
}

half4 main(float2 p) {
    float2 uv = (p - 0.5 * size) / min(size.x, size.y) + 0.5;
    float r = 0.45 + 0.3 * level;
    float2 a = float2(0.5 + 0.28 * sin(time * 0.7), 0.5 + 0.28 * cos(time * 0.9));
    float2 b = float2(0.5 + 0.26 * sin(time * 0.6 + 1.7), 0.5 + 0.26 * cos(time * 0.8 + 1.7));
    float2 c = float2(0.5 + 0.24 * sin(time * 1.1 + 3.1), 0.5 + 0.24 * cos(time * 0.5 + 3.1));
    half4 color = bg;
    color = mix(color, c1, half(blob(uv, a, r)));
    color = mix(color, c2, half(0.85 * blob(uv, b, r)));
    color = mix(color, c3, half(0.7 * blob(uv, c, r * 0.9)));
    return color;
}
"""
