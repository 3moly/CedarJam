package vector
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val BarLeft: ImageVector
    get() {
        if (_BarLeft != null) {
            return _BarLeft!!
        }
        _BarLeft = ImageVector.Builder(
            name = "BarLeft",
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
                moveTo(9f, 20f)
                verticalLineTo(4f)
                moveTo(9f, 20f)
                horizontalLineTo(16.803f)
                curveTo(17.921f, 20f, 18.48f, 20f, 18.907f, 19.782f)
                curveTo(19.284f, 19.59f, 19.59f, 19.284f, 19.782f, 18.907f)
                curveTo(20f, 18.48f, 20f, 17.921f, 20f, 16.803f)
                verticalLineTo(7.197f)
                curveTo(20f, 6.079f, 20f, 5.519f, 19.782f, 5.092f)
                curveTo(19.59f, 4.715f, 19.284f, 4.41f, 18.907f, 4.218f)
                curveTo(18.48f, 4f, 17.92f, 4f, 16.8f, 4f)
                horizontalLineTo(9f)
                moveTo(9f, 20f)
                horizontalLineTo(7.197f)
                curveTo(6.079f, 20f, 5.519f, 20f, 5.092f, 19.782f)
                curveTo(4.715f, 19.59f, 4.41f, 19.284f, 4.218f, 18.907f)
                curveTo(4f, 18.48f, 4f, 17.92f, 4f, 16.8f)
                verticalLineTo(7.2f)
                curveTo(4f, 6.08f, 4f, 5.52f, 4.218f, 5.092f)
                curveTo(4.41f, 4.715f, 4.715f, 4.41f, 5.092f, 4.218f)
                curveTo(5.52f, 4f, 6.08f, 4f, 7.2f, 4f)
                horizontalLineTo(9f)
            }
        }.build()

        return _BarLeft!!
    }

@Suppress("ObjectPropertyName")
private var _BarLeft: ImageVector? = null