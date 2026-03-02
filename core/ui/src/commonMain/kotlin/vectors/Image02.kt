package vectors

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val Image02: ImageVector
    get() {
        if (_Image02 != null) {
            return _Image02!!
        }
        _Image02 = ImageVector.Builder(
            name = "Image02",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(4.8f, 21.6f)
                horizontalLineTo(19.2f)
                curveTo(20.525f, 21.6f, 21.6f, 20.469f, 21.6f, 19.074f)
                verticalLineTo(4.926f)
                curveTo(21.6f, 3.531f, 20.525f, 2.4f, 19.2f, 2.4f)
                horizontalLineTo(4.8f)
                curveTo(3.474f, 2.4f, 2.4f, 3.531f, 2.4f, 4.926f)
                verticalLineTo(19.074f)
                curveTo(2.4f, 20.469f, 3.474f, 21.6f, 4.8f, 21.6f)
                close()
            }
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(6f, 15.6f)
                horizontalLineTo(18f)
                lineTo(14f, 8.6f)
                lineTo(11f, 13.1f)
                lineTo(9f, 11.1f)
                lineTo(6f, 15.6f)
                close()
            }
        }.build()

        return _Image02!!
    }

@Suppress("ObjectPropertyName")
private var _Image02: ImageVector? = null
