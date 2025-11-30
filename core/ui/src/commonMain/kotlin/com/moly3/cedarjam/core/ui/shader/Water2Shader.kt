package com.moly3.cedarjam.core.ui.shader

import androidx.compose.ui.graphics.Color
import com.mikepenz.hypnoticcanvas.RuntimeEffect
import com.mikepenz.hypnoticcanvas.shaders.Shader

class Water2Shader : Shader {
    override val name: String
        get() = "-"

    override val authorName: String
        get() = "-"

    override val authorUrl: String
        get() = "-"

    override val credit: String
        get() = "-"

    override val license: String
        get() = "-"

    override val licenseUrl: String
        get() = "-"

    override val speedModifier: Float
        get() = 0.1f

    override val sksl = """
uniform float uTime;
uniform vec3 uResolution;
uniform vec3 uBaseColor;
uniform vec3 uBackColor; // background color

const float PI = 3.14159;

vec2 hash22(vec2 p) {
    return fract(sin(vec2(
        dot(p, vec2(127.1, 311.7)), 
        dot(p, vec2(269.5, 183.3))
    )) * 43758.5453123);
}

float circle(vec2 uv, vec2 c, float s, float w) {
    float x = abs(length(uv - c) - s) * w;
    return clamp(1.0 - x, 0.0, 1.0);
}

float voronoi(vec2 uv, float tileCount) {
    vec2 tile = floor(uv * tileCount);
    vec2 tuv = fract(uv * tileCount);

    float dist = 10.0;
    for (int x = -1; x <= 1; x++) {
        for (int y = -1; y <= 1; y++) {
            vec2 offset = vec2(float(x), float(y));
            vec2 point = hash22(tile + offset) + offset;
            float oldDist = dist;
            dist = length(tuv - point);
            dist = min(dist, oldDist);
        }
    }
    return dist;
}

float fbm(vec2 uv, float f) {
    float dist = 0.0;
    float amp = 1.0;
    float freq = f;
    float ampFact = 0.5;
    float freqFact = 1.5;
    float maxDist = 0.0;
    vec2 p = vec2(113.123, 55.1);

    // fixed 10 iterations
    for (int i = 0; i < 10; i++) {
        vec2 m = (hash22(vec2(float(i) * f))) - 0.5;
        dist += voronoi(uv + m * uTime * 0.05 * float(i+1), freq) * amp;
        maxDist += amp;
        amp *= ampFact;
        freq *= freqFact;
        uv += p;
    }
    return dist / maxDist;
}

vec4 main(vec2 fragCoord)
{
    vec2 uv = fragCoord / uResolution.y;
    float t = uTime;

    float p = fbm(uv, 2.0);
    float q = fbm(uv + vec2(p, p), 2.0);
    float d = fbm(uv + vec2(q, q * 0.2), 1.0);

    // modulation factor: controls brightness variation (darker range)
    float intensity = 0.3 + 0.3 * cos(d * 6.0 + t * 0.2);

    // base color modulation
    vec3 col = uBaseColor * intensity;

    float add = 0.0;
    for (int i = 0; i < 5; i++) {
        float tl = (float(i) + fract(t));
        vec2 r = hash22(vec2(floor(t) - float(i))) * uResolution.xy / uResolution.y;
        float tf = smoothstep(0.0, 1.0, tl);
        if (tf >= 1.0) {
            tf = 1.0 - smoothstep(0.1, 5.0, tl);
        }
        add += circle(uv + (d - 0.5) * tl * 0.4, r, tl * 0.1, 20.0 / tl) * tf;
    }

    // instead of mixing with black, mix with background color
    col = mix(uBackColor, col, clamp(add, 0.0, 1.0));

    return vec4(col, 1.0);
}
    """

    private var activeColor: Color = Color.White

    fun setActiveColor(color: Color) {
        activeColor = color
    }

    private fun RuntimeEffect.setColor(name: String, color: Color) {
        this.setFloatUniform(name, color.red, color.green, color.blue)
    }

    override fun applyUniforms(
        runtimeEffect: RuntimeEffect,
        time: Float,
        width: Float,
        height: Float
    ) {
        super.applyUniforms(runtimeEffect, time, width, height)

        val color = activeColor

        runtimeEffect.setColor("uBaseColor", color)
        runtimeEffect.setColor("uBackColor", Color.Black)
    }
}