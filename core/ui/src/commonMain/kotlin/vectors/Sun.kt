package vectors

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val Sun: ImageVector
    get() {
        if (_Sun != null) {
            return _Sun!!
        }
        _Sun = ImageVector.Builder(
            name = "Sun",
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
                moveTo(12f, 4f)
                verticalLineTo(2f)
                moveTo(12f, 20f)
                verticalLineTo(22f)
                moveTo(6.414f, 6.414f)
                lineTo(5f, 5f)
                moveTo(17.728f, 17.728f)
                lineTo(19.142f, 19.142f)
                moveTo(4f, 12f)
                horizontalLineTo(2f)
                moveTo(20f, 12f)
                horizontalLineTo(22f)
                moveTo(17.729f, 6.414f)
                lineTo(19.143f, 5f)
                moveTo(6.415f, 17.728f)
                lineTo(5f, 19.142f)
                moveTo(12f, 17f)
                curveTo(9.239f, 17f, 7f, 14.761f, 7f, 12f)
                curveTo(7f, 9.239f, 9.239f, 7f, 12f, 7f)
                curveTo(14.761f, 7f, 17f, 9.239f, 17f, 12f)
                curveTo(17f, 14.761f, 14.761f, 17f, 12f, 17f)
                close()
            }
        }.build()

        return _Sun!!
    }

@Suppress("ObjectPropertyName")
private var _Sun: ImageVector? = null
