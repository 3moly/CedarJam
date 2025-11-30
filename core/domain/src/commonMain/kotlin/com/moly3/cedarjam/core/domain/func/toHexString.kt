package com.moly3.cedarjam.core.domain.func

import androidx.compose.ui.graphics.Color

fun Color.toHexString(): String {
    val a = (alpha * 255).toInt().toUByte()
    val r = (red * 255).toInt().toUByte()
    val g = (green * 255).toInt().toUByte()
    val b = (blue * 255).toInt().toUByte()

    return "#" + a.toString(16).padStart(2, '0') +
            r.toString(16).padStart(2, '0') +
            g.toString(16).padStart(2, '0') +
            b.toString(16).padStart(2, '0')
                .uppercase()
}

fun String.toColor(): Color {
    return try {
        val cleaned = removePrefix("#")
        require(cleaned.length == 8) { "Color string must be in format #AARRGGBB" }

        val a = cleaned.substring(0, 2).toInt(16) / 255f
        val r = cleaned.substring(2, 4).toInt(16) / 255f
        val g = cleaned.substring(4, 6).toInt(16) / 255f
        val b = cleaned.substring(6, 8).toInt(16) / 255f
        Color(r, g, b, a)
    } catch (exc: Exception) {
        Color.Unspecified
    }
}