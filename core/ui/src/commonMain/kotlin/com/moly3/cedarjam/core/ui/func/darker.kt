package com.moly3.cedarjam.core.ui.func

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.moly3.cedarjam.core.ui.compositions.LocalAppTheme

fun Color.darker(factor: Float = 0.8f): Color {
    return Color(
        red = (red * factor).coerceIn(0f, 1f),
        green = (green * factor).coerceIn(0f, 1f),
        blue = (blue * factor).coerceIn(0f, 1f),
        alpha = alpha // preserve original alpha
    )
}

//@Composable
//fun Color.darkerOrLighter(factor: Float = 0.8f): Color {
//    val backgroundPrimary = LocalAppTheme.current.colors.backgroundPrimary
//    return Color(
//        red = (red * factor).coerceIn(0f, 1f),
//        green = (green * factor).coerceIn(0f, 1f),
//        blue = (blue * factor).coerceIn(0f, 1f),
//        alpha = alpha // preserve original alpha
//    )
//}