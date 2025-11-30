package com.moly3.cedarjam.core.ui.shader

import androidx.compose.ui.graphics.Color
import com.mikepenz.hypnoticcanvas.RuntimeEffect
import com.mikepenz.hypnoticcanvas.shaders.Shader
import com.moly3.cedarjam.core.ui.func.darker

class WaterShader : Shader {
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
uniform vec3 uColor1; // first base color
uniform vec3 uColor2; // second base color

const float TILING_FACTOR = 1.0;
const int MAX_ITER = 8;
const float TAU = 6.28318530718;

float waterHighlight(vec2 p, float time, float foaminess)
{
    vec2 i = vec2(p);
    float c = 0.0;
    float foaminess_factor = mix(1.0, 6.0, foaminess);
    float inten = 0.005 * foaminess_factor;

    for (int n = 0; n < MAX_ITER; n++) 
    {
        float t = time * (1.0 - (3.5 / float(n+1)));
        i = p + vec2(cos(t - i.x) + sin(t + i.y),
                     sin(t - i.y) + cos(t + i.x));
        c += 1.0 / length(vec2(p.x / (sin(i.x+t)), 
                               p.y / (cos(i.y+t))));
    }

    c = 0.2 + c / (inten * float(MAX_ITER));
    c = 1.17 - pow(c, 1.4);
    c = pow(abs(c), 8.0);
    return c / sqrt(foaminess_factor);
}

vec4 main(vec2 fragCoord)
{
    float time = uTime * 0.1 + 23.0;

    vec2 uv = fragCoord.xy / uResolution.xy;
    vec2 uv_square = vec2(uv.x * uResolution.x / uResolution.y, uv.y);
    float dist_center = pow(2.0 * length(uv - 0.5), 2.0);

    float foaminess = smoothstep(0.4, 1.8, dist_center);
    float clearness = 0.1 + 0.9 * smoothstep(0.1, 0.5, dist_center);

    vec2 p = mod(uv_square * TAU * TILING_FACTOR, TAU) - 250.0;

    float c = waterHighlight(p, time, foaminess);

    vec3 water_color = uColor1; // instead of fixed vec3(0.0, 0.35, 0.5)
    vec3 foam_color  = uColor2; // highlight foam variation

    vec3 color = vec3(c);
    color = clamp(color + water_color, 0.0, 1.0);

    color = mix(water_color, color, clearness);
    color = mix(color, foam_color, foaminess * 0.2);

    return vec4(color, 1.0);
}
    """

    private var activeColor: Color = Color.White

    fun setActiveColor(color: Color) {
        activeColor = color
    }

    override fun applyUniforms(
        runtimeEffect: RuntimeEffect,
        time: Float,
        width: Float,
        height: Float
    ) {
        super.applyUniforms(runtimeEffect, time, width, height)

        val color = activeColor
        val color2 = color.darker(0.4f)

        runtimeEffect.setFloatUniform("uColor1", color.red, color.green, color.blue)
        runtimeEffect.setFloatUniform("uColor2", color2.red, color2.green, color2.blue)
    }
}