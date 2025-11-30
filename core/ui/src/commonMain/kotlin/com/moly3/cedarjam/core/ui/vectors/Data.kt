package com.moly3.cedarjam.core.ui.vectors
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val Data: ImageVector
    get() {
        if (_Data != null) {
            return _Data!!
        }
        _Data = ImageVector.Builder(
            name = "Data",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color.White),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(18f, 12f)
                verticalLineTo(17f)
                curveTo(18f, 18.657f, 15.314f, 20f, 12f, 20f)
                curveTo(8.686f, 20f, 6f, 18.657f, 6f, 17f)
                verticalLineTo(12f)
                moveTo(18f, 12f)
                verticalLineTo(7f)
                moveTo(18f, 12f)
                curveTo(18f, 13.657f, 15.314f, 15f, 12f, 15f)
                curveTo(8.686f, 15f, 6f, 13.657f, 6f, 12f)
                moveTo(18f, 7f)
                curveTo(18f, 5.343f, 15.314f, 4f, 12f, 4f)
                curveTo(8.686f, 4f, 6f, 5.343f, 6f, 7f)
                moveTo(18f, 7f)
                curveTo(18f, 8.657f, 15.314f, 10f, 12f, 10f)
                curveTo(8.686f, 10f, 6f, 8.657f, 6f, 7f)
                moveTo(6f, 12f)
                verticalLineTo(7f)
            }
        }.build()

        return _Data!!
    }

@Suppress("ObjectPropertyName")
private var _Data: ImageVector? = null