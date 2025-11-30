package com.moly3.cedarjam.core.ui.vectors
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val FolderAdd: ImageVector
    get() {
        if (_FolderAdd != null) {
            return _FolderAdd!!
        }
        _FolderAdd = ImageVector.Builder(
            name = "FolderAdd",
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
                moveTo(12f, 16f)
                verticalLineTo(13f)
                moveTo(12f, 13f)
                verticalLineTo(10f)
                moveTo(12f, 13f)
                horizontalLineTo(9f)
                moveTo(12f, 13f)
                horizontalLineTo(15f)
                moveTo(3f, 6f)
                verticalLineTo(16.8f)
                curveTo(3f, 17.92f, 3f, 18.48f, 3.218f, 18.908f)
                curveTo(3.41f, 19.284f, 3.715f, 19.59f, 4.092f, 19.782f)
                curveTo(4.519f, 20f, 5.079f, 20f, 6.197f, 20f)
                horizontalLineTo(17.803f)
                curveTo(18.921f, 20f, 19.48f, 20f, 19.907f, 19.782f)
                curveTo(20.284f, 19.59f, 20.59f, 19.284f, 20.782f, 18.908f)
                curveTo(21f, 18.48f, 21f, 17.92f, 21f, 16.8f)
                lineTo(21f, 9.2f)
                curveTo(21f, 8.08f, 21f, 7.52f, 20.782f, 7.092f)
                curveTo(20.59f, 6.715f, 20.284f, 6.41f, 19.908f, 6.218f)
                curveTo(19.48f, 6f, 18.92f, 6f, 17.8f, 6f)
                horizontalLineTo(12f)
                moveTo(3f, 6f)
                horizontalLineTo(12f)
                moveTo(3f, 6f)
                curveTo(3f, 4.895f, 3.895f, 4f, 5f, 4f)
                horizontalLineTo(8.675f)
                curveTo(9.164f, 4f, 9.409f, 4f, 9.639f, 4.055f)
                curveTo(9.843f, 4.104f, 10.038f, 4.185f, 10.217f, 4.295f)
                curveTo(10.419f, 4.419f, 10.592f, 4.592f, 10.938f, 4.938f)
                lineTo(12f, 6f)
            }
        }.build()

        return _FolderAdd!!
    }

@Suppress("ObjectPropertyName")
private var _FolderAdd: ImageVector? = null