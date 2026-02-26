package vectors

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val AddRow: ImageVector
    get() {
        if (_AddRow != null) {
            return _AddRow!!
        }
        _AddRow = ImageVector.Builder(
            name = "AddRow",
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
                moveTo(3f, 14f)
                verticalLineTo(15f)
                curveTo(3f, 16.105f, 3.895f, 17f, 5f, 17f)
                lineTo(19f, 17f)
                curveTo(20.105f, 17f, 21f, 16.105f, 21f, 15f)
                lineTo(21f, 13f)
                curveTo(21f, 11.895f, 20.105f, 11f, 19f, 11f)
                horizontalLineTo(13f)
                moveTo(10f, 8f)
                horizontalLineTo(7f)
                moveTo(7f, 8f)
                horizontalLineTo(4f)
                moveTo(7f, 8f)
                verticalLineTo(5f)
                moveTo(7f, 8f)
                verticalLineTo(11f)
            }
        }.build()

        return _AddRow!!
    }

@Suppress("ObjectPropertyName")
private var _AddRow: ImageVector? = null
