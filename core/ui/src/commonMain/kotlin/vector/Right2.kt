package vector

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val Right2: ImageVector
    get() {
        if (_Right2 != null) {
            return _Right2!!
        }
        _Right2 = ImageVector.Builder(
            name = "Right2",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color.White),
                strokeLineWidth = 1.5f,
                strokeLineCap = StrokeCap.Round
            ) {
                moveTo(9f, 19f)
                lineTo(14.331f, 12.781f)
                curveTo(14.716f, 12.332f, 14.716f, 11.668f, 14.331f, 11.219f)
                lineTo(9f, 5f)
            }
        }.build()

        return _Right2!!
    }

@Suppress("ObjectPropertyName")
private var _Right2: ImageVector? = null
