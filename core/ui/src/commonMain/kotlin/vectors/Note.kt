package vectors

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val Note: ImageVector
    get() {
        if (_Note != null) {
            return _Note!!
        }
        _Note = ImageVector.Builder(
            name = "Note",
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
                moveTo(13f, 19.999f)
                curveTo(12.905f, 20f, 12.799f, 20f, 12.677f, 20f)
                horizontalLineTo(7.197f)
                curveTo(6.079f, 20f, 5.519f, 20f, 5.092f, 19.782f)
                curveTo(4.715f, 19.59f, 4.41f, 19.284f, 4.218f, 18.908f)
                curveTo(4f, 18.48f, 4f, 17.92f, 4f, 16.8f)
                verticalLineTo(7.2f)
                curveTo(4f, 6.08f, 4f, 5.52f, 4.218f, 5.092f)
                curveTo(4.41f, 4.715f, 4.715f, 4.41f, 5.092f, 4.218f)
                curveTo(5.52f, 4f, 6.08f, 4f, 7.2f, 4f)
                horizontalLineTo(16.8f)
                curveTo(17.92f, 4f, 18.48f, 4f, 18.907f, 4.218f)
                curveTo(19.284f, 4.41f, 19.59f, 4.715f, 19.782f, 5.092f)
                curveTo(20f, 5.519f, 20f, 6.079f, 20f, 7.197f)
                verticalLineTo(12.675f)
                curveTo(20f, 12.797f, 20f, 12.905f, 19.999f, 13f)
                moveTo(13f, 19.999f)
                curveTo(13.286f, 19.997f, 13.466f, 19.986f, 13.639f, 19.945f)
                curveTo(13.843f, 19.896f, 14.038f, 19.815f, 14.217f, 19.705f)
                curveTo(14.419f, 19.581f, 14.592f, 19.409f, 14.938f, 19.063f)
                lineTo(19.063f, 14.938f)
                curveTo(19.409f, 14.592f, 19.581f, 14.419f, 19.705f, 14.217f)
                curveTo(19.814f, 14.038f, 19.895f, 13.842f, 19.944f, 13.638f)
                curveTo(19.986f, 13.466f, 19.996f, 13.285f, 19.999f, 13f)
                moveTo(13f, 19.999f)
                verticalLineTo(14.6f)
                curveTo(13f, 14.04f, 13f, 13.76f, 13.109f, 13.546f)
                curveTo(13.205f, 13.358f, 13.358f, 13.205f, 13.546f, 13.109f)
                curveTo(13.76f, 13f, 14.04f, 13f, 14.6f, 13f)
                horizontalLineTo(19.999f)
            }
        }.build()

        return _Note!!
    }

@Suppress("ObjectPropertyName")
private var _Note: ImageVector? = null