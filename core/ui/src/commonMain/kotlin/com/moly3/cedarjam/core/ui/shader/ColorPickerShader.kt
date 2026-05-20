package com.moly3.cedarjam.core.ui.shader

import androidx.compose.ui.graphics.Color
import com.moly3.shaders.RuntimeEffect
import com.moly3.shaders.Shader

/**
 * A color-picker shader. Renders the full HSV color space on a 2D surface:
 *   - X axis  -> hue (0..1 wraps the color wheel)
 *   - Y axis  -> saturation (top = white/desaturated, bottom = full saturation)
 *   - uValue  -> overall brightness/value (0..1), controlled from outside
 *
 * Output at any (x, y) is the exact color at that point — no noise, no time,
 * so sampling is stable and round-trips cleanly with hsvToColor() in Kotlin.
 */
class ColorPickerShader : Shader {
    override val name: String get() = "color-picker"

    // No animation — picker should be still.
    override val speedModifier: Float get() = 0f

    override val sksl = """
uniform float uTime;
uniform vec3  uResolution;
uniform float uValue;

vec3 hsv2rgb(vec3 c) {
    vec4 K = vec4(1.0, 2.0 / 3.0, 1.0 / 3.0, 3.0);
    vec3 p = abs(fract(c.xxx + K.xyz) * 6.0 - K.www);
    return c.z * mix(K.xxx, clamp(p - K.xxx, 0.0, 1.0), c.y);
}

vec4 main(vec2 fragCoord) {
    vec2 uv = fragCoord / uResolution.xy;

    float hue        = uv.x;
    float saturation = uv.y;
    float val        = clamp(uValue, 0.0, 1.0);

    vec3 rgb = hsv2rgb(vec3(hue, saturation, val));
    return vec4(rgb, 1.0);
}
    """

    private var value: Float = 1f

    /** Brightness / V component, 0..1. Drive this from a separate slider. */
    fun setValue(v: Float) {
        value = v.coerceIn(0f, 1f)
    }

    override fun applyUniforms(
        runtimeEffect: RuntimeEffect,
        time: Float,
        width: Float,
        height: Float
    ) {
        super.applyUniforms(runtimeEffect, time, width, height)
        runtimeEffect.setFloatUniform("uValue", value)
    }

    companion object {
        /**
         * Mirrors what the shader does on the GPU, so you can compute the
         * exact selected color from a normalized (x, y) position without
         * reading back pixels. x, y, v all in 0..1.
         */
        fun colorAt(x: Float, y: Float, v: Float = 1f): Color {
            val h = ((x % 1f) + 1f) % 1f
            val s = y.coerceIn(0f, 1f)
            val vv = v.coerceIn(0f, 1f)

            val i = (h * 6f).toInt()
            val f = h * 6f - i
            val p = vv * (1f - s)
            val q = vv * (1f - f * s)
            val t = vv * (1f - (1f - f) * s)

            val (r, g, b) = when (i % 6) {
                0 -> Triple(vv, t, p)
                1 -> Triple(q, vv, p)
                2 -> Triple(p, vv, t)
                3 -> Triple(p, q, vv)
                4 -> Triple(t, p, vv)
                else -> Triple(vv, p, q)
            }
            return Color(r, g, b, 1f)
        }
    }
}