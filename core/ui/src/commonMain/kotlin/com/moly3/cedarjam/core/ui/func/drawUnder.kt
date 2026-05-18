package com.moly3.cedarjam.core.ui.func

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun Modifier.drawUnder(
    borderColor: Color = Color.Gray,
    borderThickness: Dp = 1.dp
): Modifier {
    return this.drawBehind {
        val stroke = borderThickness.toPx()
        drawLine(
            color = borderColor,
            start = Offset(0f, size.height - stroke / 2f),
            end = Offset(size.width, size.height - stroke / 2f),
            strokeWidth = stroke
        )
    }
}