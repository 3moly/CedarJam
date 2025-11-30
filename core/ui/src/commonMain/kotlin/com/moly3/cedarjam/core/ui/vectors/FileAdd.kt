package com.moly3.cedarjam.core.ui.vectors

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val FileAdd: ImageVector
    get() {
        if (_FileAdd != null) {
            return _FileAdd!!
        }
        _FileAdd = ImageVector.Builder(
            name = "FileAdd",
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
                moveTo(12f, 18f)
                verticalLineTo(15f)
                moveTo(12f, 15f)
                verticalLineTo(12f)
                moveTo(12f, 15f)
                horizontalLineTo(9f)
                moveTo(12f, 15f)
                horizontalLineTo(15f)
                moveTo(13f, 3.001f)
                curveTo(12.905f, 3f, 12.797f, 3f, 12.675f, 3f)
                horizontalLineTo(8.2f)
                curveTo(7.08f, 3f, 6.52f, 3f, 6.092f, 3.218f)
                curveTo(5.715f, 3.41f, 5.41f, 3.715f, 5.218f, 4.092f)
                curveTo(5f, 4.52f, 5f, 5.08f, 5f, 6.2f)
                verticalLineTo(17.8f)
                curveTo(5f, 18.92f, 5f, 19.48f, 5.218f, 19.908f)
                curveTo(5.41f, 20.284f, 5.715f, 20.59f, 6.092f, 20.782f)
                curveTo(6.519f, 21f, 7.079f, 21f, 8.197f, 21f)
                lineTo(15.803f, 21f)
                curveTo(16.921f, 21f, 17.48f, 21f, 17.907f, 20.782f)
                curveTo(18.284f, 20.59f, 18.59f, 20.284f, 18.782f, 19.908f)
                curveTo(19f, 19.48f, 19f, 18.921f, 19f, 17.804f)
                verticalLineTo(9.326f)
                curveTo(19f, 9.203f, 19f, 9.096f, 18.999f, 9f)
                moveTo(13f, 3.001f)
                curveTo(13.286f, 3.003f, 13.466f, 3.014f, 13.639f, 3.055f)
                curveTo(13.843f, 3.104f, 14.038f, 3.185f, 14.217f, 3.295f)
                curveTo(14.419f, 3.419f, 14.592f, 3.592f, 14.938f, 3.938f)
                lineTo(18.063f, 7.063f)
                curveTo(18.409f, 7.409f, 18.581f, 7.581f, 18.705f, 7.783f)
                curveTo(18.814f, 7.962f, 18.895f, 8.157f, 18.944f, 8.361f)
                curveTo(18.986f, 8.534f, 18.996f, 8.715f, 18.999f, 9f)
                moveTo(13f, 3.001f)
                verticalLineTo(5.8f)
                curveTo(13f, 6.92f, 13f, 7.48f, 13.218f, 7.908f)
                curveTo(13.41f, 8.284f, 13.715f, 8.59f, 14.092f, 8.782f)
                curveTo(14.519f, 9f, 15.079f, 9f, 16.197f, 9f)
                horizontalLineTo(18.999f)
                moveTo(18.999f, 9f)
                horizontalLineTo(19f)
            }
        }.build()

        return _FileAdd!!
    }

@Suppress("ObjectPropertyName")
private var _FileAdd: ImageVector? = null