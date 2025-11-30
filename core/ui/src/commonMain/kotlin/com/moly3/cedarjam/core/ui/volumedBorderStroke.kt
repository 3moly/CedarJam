package com.moly3.cedarjam.core.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

val volumedBorderStroke = BorderStroke(
    width = 1.dp,
    brush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF545454), // top lighter
            Color(0xFF272727) // bottom darker
        ),
        endY = 50f
    )
)