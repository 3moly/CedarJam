package com.moly3.cedarjam.core.ui.vectors

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val DummySquareSmall: ImageVector
    get() {
        if (_DummySquareSmall != null) {
            return _DummySquareSmall!!
        }
        _DummySquareSmall = ImageVector.Builder(
            name = "DummySquareSmall",
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
                moveTo(8f, 11.2f)
                verticalLineTo(12.8f)
                curveTo(8f, 13.92f, 8f, 14.48f, 8.218f, 14.908f)
                curveTo(8.41f, 15.284f, 8.715f, 15.59f, 9.092f, 15.782f)
                curveTo(9.519f, 16f, 10.079f, 16f, 11.197f, 16f)
                horizontalLineTo(12.803f)
                curveTo(13.921f, 16f, 14.48f, 16f, 14.907f, 15.782f)
                curveTo(15.284f, 15.59f, 15.59f, 15.284f, 15.782f, 14.908f)
                curveTo(16f, 14.481f, 16f, 13.922f, 16f, 12.804f)
                verticalLineTo(11.197f)
                curveTo(16f, 10.079f, 16f, 9.519f, 15.782f, 9.092f)
                curveTo(15.59f, 8.715f, 15.284f, 8.41f, 14.907f, 8.218f)
                curveTo(14.48f, 8f, 13.92f, 8f, 12.8f, 8f)
                horizontalLineTo(11.2f)
                curveTo(10.08f, 8f, 9.52f, 8f, 9.092f, 8.218f)
                curveTo(8.715f, 8.41f, 8.41f, 8.715f, 8.218f, 9.092f)
                curveTo(8f, 9.52f, 8f, 10.08f, 8f, 11.2f)
                close()
            }
        }.build()

        return _DummySquareSmall!!
    }

@Suppress("ObjectPropertyName")
private var _DummySquareSmall: ImageVector? = null
