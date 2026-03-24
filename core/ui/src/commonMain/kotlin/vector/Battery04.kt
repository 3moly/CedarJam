package vector

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val Battery04: ImageVector
    get() {
        if (_Battery04 != null) {
            return _Battery04!!
        }
        _Battery04 = ImageVector.Builder(
            name = "Battery04",
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
                moveTo(22.2f, 13.8f)
                verticalLineTo(10.2f)
                moveTo(10.2f, 15f)
                lineTo(13.2f, 12f)
                horizontalLineTo(7.8f)
                lineTo(10.8f, 9f)
                moveTo(4.2f, 18f)
                horizontalLineTo(16.2f)
                curveTo(17.525f, 18f, 18.6f, 16.926f, 18.6f, 15.6f)
                verticalLineTo(8.4f)
                curveTo(18.6f, 7.075f, 17.525f, 6f, 16.2f, 6f)
                horizontalLineTo(4.2f)
                curveTo(2.875f, 6f, 1.8f, 7.075f, 1.8f, 8.4f)
                verticalLineTo(15.6f)
                curveTo(1.8f, 16.926f, 2.875f, 18f, 4.2f, 18f)
                close()
            }
        }.build()

        return _Battery04!!
    }

@Suppress("ObjectPropertyName")
private var _Battery04: ImageVector? = null
