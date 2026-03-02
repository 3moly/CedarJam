package vectors

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val Transform: ImageVector
    get() {
        if (_Transform != null) {
            return _Transform!!
        }
        _Transform = ImageVector.Builder(
            name = "Transform",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round
            ) {
                moveTo(19.074f, 16.547f)
                verticalLineTo(7.453f)
                moveTo(16.547f, 19.074f)
                horizontalLineTo(7.958f)
                moveTo(4.926f, 16.295f)
                verticalLineTo(7.705f)
                moveTo(7.453f, 4.926f)
                horizontalLineTo(16.295f)
                moveTo(3.41f, 7.453f)
                horizontalLineTo(6.442f)
                curveTo(7f, 7.453f, 7.453f, 7f, 7.453f, 6.442f)
                verticalLineTo(3.41f)
                curveTo(7.453f, 2.852f, 7f, 2.4f, 6.442f, 2.4f)
                horizontalLineTo(3.41f)
                curveTo(2.852f, 2.4f, 2.4f, 2.852f, 2.4f, 3.41f)
                verticalLineTo(6.442f)
                curveTo(2.4f, 7f, 2.852f, 7.453f, 3.41f, 7.453f)
                close()
                moveTo(17.558f, 7.453f)
                horizontalLineTo(20.589f)
                curveTo(21.147f, 7.453f, 21.6f, 7f, 21.6f, 6.442f)
                verticalLineTo(3.41f)
                curveTo(21.6f, 2.852f, 21.147f, 2.4f, 20.589f, 2.4f)
                horizontalLineTo(17.558f)
                curveTo(17f, 2.4f, 16.547f, 2.852f, 16.547f, 3.41f)
                verticalLineTo(6.442f)
                curveTo(16.547f, 7f, 17f, 7.453f, 17.558f, 7.453f)
                close()
                moveTo(3.41f, 21.6f)
                horizontalLineTo(6.442f)
                curveTo(7f, 21.6f, 7.453f, 21.147f, 7.453f, 20.589f)
                verticalLineTo(17.558f)
                curveTo(7.453f, 17f, 7f, 16.547f, 6.442f, 16.547f)
                horizontalLineTo(3.41f)
                curveTo(2.852f, 16.547f, 2.4f, 17f, 2.4f, 17.558f)
                verticalLineTo(20.589f)
                curveTo(2.4f, 21.147f, 2.852f, 21.6f, 3.41f, 21.6f)
                close()
                moveTo(17.558f, 21.6f)
                horizontalLineTo(20.589f)
                curveTo(21.147f, 21.6f, 21.6f, 21.147f, 21.6f, 20.589f)
                verticalLineTo(17.558f)
                curveTo(21.6f, 17f, 21.147f, 16.547f, 20.589f, 16.547f)
                horizontalLineTo(17.558f)
                curveTo(17f, 16.547f, 16.547f, 17f, 16.547f, 17.558f)
                verticalLineTo(20.589f)
                curveTo(16.547f, 21.147f, 17f, 21.6f, 17.558f, 21.6f)
                close()
            }
        }.build()

        return _Transform!!
    }

@Suppress("ObjectPropertyName")
private var _Transform: ImageVector? = null
