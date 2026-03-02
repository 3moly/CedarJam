package vectors

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val Annotation: ImageVector
    get() {
        if (_Annotation != null) {
            return _Annotation!!
        }
        _Annotation = ImageVector.Builder(
            name = "Annotation",
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
                moveTo(12f, 21f)
                lineTo(14.446f, 16.304f)
                horizontalLineTo(19f)
                curveTo(20.105f, 16.304f, 21f, 15.409f, 21f, 14.304f)
                verticalLineTo(5f)
                curveTo(21f, 3.895f, 20.105f, 3f, 19f, 3f)
                horizontalLineTo(5f)
                curveTo(3.895f, 3f, 3f, 3.895f, 3f, 5f)
                verticalLineTo(14.304f)
                curveTo(3f, 15.409f, 3.895f, 16.304f, 5f, 16.304f)
                horizontalLineTo(9.75f)
                lineTo(12f, 21f)
                close()
            }
        }.build()

        return _Annotation!!
    }

@Suppress("ObjectPropertyName")
private var _Annotation: ImageVector? = null
