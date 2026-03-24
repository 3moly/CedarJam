package vector

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val WaterDrop: ImageVector
    get() {
        if (_WaterDrop != null) {
            return _WaterDrop!!
        }
        _WaterDrop = ImageVector.Builder(
            name = "WaterDrop",
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
                moveTo(16f, 13.385f)
                curveTo(16f, 14.609f, 15.526f, 15.783f, 14.682f, 16.648f)
                curveTo(14.203f, 17.14f, 13.627f, 17.509f, 13f, 17.736f)
                moveTo(19f, 13.692f)
                curveTo(19f, 7.115f, 12f, 2f, 12f, 2f)
                curveTo(12f, 2f, 5f, 7.115f, 5f, 13.692f)
                curveTo(5f, 15.63f, 5.738f, 17.489f, 7.05f, 18.86f)
                curveTo(8.363f, 20.23f, 10.144f, 20.999f, 12f, 20.999f)
                curveTo(13.857f, 20.999f, 15.637f, 20.23f, 16.95f, 18.859f)
                curveTo(18.263f, 17.489f, 19f, 15.63f, 19f, 13.692f)
                close()
            }
        }.build()

        return _WaterDrop!!
    }

@Suppress("ObjectPropertyName")
private var _WaterDrop: ImageVector? = null
