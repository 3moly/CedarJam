package com.moly3.cedarjam.core.ui.vectors

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val Left2: ImageVector
    get() {
        if (_Left2 != null) {
            return _Left2!!
        }
        _Left2 = ImageVector.Builder(
            name = "Left2",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color.White),
                strokeLineWidth = 1.5f,
                strokeLineCap = StrokeCap.Round
            ) {
                moveTo(15f, 5f)
                lineTo(9.669f, 11.219f)
                curveTo(9.284f, 11.668f, 9.284f, 12.332f, 9.669f, 12.781f)
                lineTo(15f, 19f)
            }
        }.build()

        return _Left2!!
    }

@Suppress("ObjectPropertyName")
private var _Left2: ImageVector? = null
