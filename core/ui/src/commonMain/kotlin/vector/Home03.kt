package vector

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val Home03: ImageVector
    get() {
        if (_Home03 != null) {
            return _Home03!!
        }
        _Home03 = ImageVector.Builder(
            name = "Home03",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f
            ) {
                moveTo(3f, 9.416f)
                curveTo(3f, 9.047f, 3.188f, 8.7f, 3.505f, 8.486f)
                lineTo(11.305f, 3.211f)
                curveTo(11.721f, 2.93f, 12.279f, 2.93f, 12.695f, 3.211f)
                lineTo(20.495f, 8.486f)
                curveTo(20.812f, 8.7f, 21f, 9.047f, 21f, 9.416f)
                verticalLineTo(19.288f)
                curveTo(21f, 20.234f, 20.194f, 21f, 19.2f, 21f)
                horizontalLineTo(4.8f)
                curveTo(3.806f, 21f, 3f, 20.234f, 3f, 19.288f)
                verticalLineTo(9.416f)
                close()
            }
        }.build()

        return _Home03!!
    }

@Suppress("ObjectPropertyName")
private var _Home03: ImageVector? = null
