package vector

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val SquareHelp: ImageVector
    get() {
        if (_SquareHelp != null) {
            return _SquareHelp!!
        }
        _SquareHelp = ImageVector.Builder(
            name = "SquareHelp",
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
                moveTo(9.146f, 9.074f)
                curveTo(9.317f, 8.547f, 9.63f, 8.079f, 10.051f, 7.719f)
                curveTo(10.471f, 7.36f, 10.984f, 7.124f, 11.53f, 7.037f)
                curveTo(12.077f, 6.95f, 12.636f, 7.016f, 13.148f, 7.228f)
                curveTo(13.659f, 7.44f, 14.101f, 7.789f, 14.427f, 8.236f)
                curveTo(14.752f, 8.684f, 14.947f, 9.213f, 14.99f, 9.764f)
                curveTo(15.034f, 10.316f, 14.924f, 10.869f, 14.673f, 11.362f)
                curveTo(14.422f, 11.855f, 14.039f, 12.269f, 13.568f, 12.558f)
                curveTo(13.096f, 12.847f, 12.553f, 13f, 12f, 13f)
                verticalLineTo(14f)
                moveTo(12.05f, 17f)
                verticalLineTo(17.1f)
                lineTo(11.95f, 17.1f)
                verticalLineTo(17f)
                horizontalLineTo(12.05f)
                close()
                moveTo(3f, 17.8f)
                verticalLineTo(6.2f)
                curveTo(3f, 5.08f, 3f, 4.52f, 3.218f, 4.092f)
                curveTo(3.41f, 3.715f, 3.715f, 3.41f, 4.092f, 3.218f)
                curveTo(4.52f, 3f, 5.08f, 3f, 6.2f, 3f)
                horizontalLineTo(17.8f)
                curveTo(18.92f, 3f, 19.48f, 3f, 19.907f, 3.218f)
                curveTo(20.284f, 3.41f, 20.59f, 3.715f, 20.782f, 4.092f)
                curveTo(21f, 4.519f, 21f, 5.079f, 21f, 6.197f)
                verticalLineTo(17.804f)
                curveTo(21f, 18.921f, 21f, 19.48f, 20.782f, 19.908f)
                curveTo(20.59f, 20.284f, 20.284f, 20.59f, 19.907f, 20.782f)
                curveTo(19.48f, 21f, 18.921f, 21f, 17.803f, 21f)
                horizontalLineTo(6.197f)
                curveTo(5.079f, 21f, 4.519f, 21f, 4.092f, 20.782f)
                curveTo(3.715f, 20.59f, 3.41f, 20.284f, 3.218f, 19.908f)
                curveTo(3f, 19.48f, 3f, 18.92f, 3f, 17.8f)
                close()
            }
        }.build()

        return _SquareHelp!!
    }

@Suppress("ObjectPropertyName")
private var _SquareHelp: ImageVector? = null