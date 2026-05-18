package vector
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val TrashEmpty: ImageVector
    get() {
        if (_TrashEmpty != null) {
            return _TrashEmpty!!
        }
        _TrashEmpty = ImageVector.Builder(
            name = "TrashEmpty",
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
                moveTo(6f, 6f)
                verticalLineTo(17.8f)
                curveTo(6f, 18.92f, 6f, 19.48f, 6.218f, 19.908f)
                curveTo(6.41f, 20.284f, 6.715f, 20.59f, 7.092f, 20.782f)
                curveTo(7.519f, 21f, 8.079f, 21f, 9.197f, 21f)
                horizontalLineTo(14.803f)
                curveTo(15.921f, 21f, 16.48f, 21f, 16.907f, 20.782f)
                curveTo(17.284f, 20.59f, 17.59f, 20.284f, 17.782f, 19.908f)
                curveTo(18f, 19.48f, 18f, 18.921f, 18f, 17.803f)
                verticalLineTo(6f)
                moveTo(6f, 6f)
                horizontalLineTo(8f)
                moveTo(6f, 6f)
                horizontalLineTo(4f)
                moveTo(8f, 6f)
                horizontalLineTo(16f)
                moveTo(8f, 6f)
                curveTo(8f, 5.068f, 8f, 4.602f, 8.152f, 4.235f)
                curveTo(8.355f, 3.745f, 8.744f, 3.355f, 9.234f, 3.152f)
                curveTo(9.602f, 3f, 10.068f, 3f, 11f, 3f)
                horizontalLineTo(13f)
                curveTo(13.932f, 3f, 14.398f, 3f, 14.765f, 3.152f)
                curveTo(15.255f, 3.355f, 15.645f, 3.745f, 15.848f, 4.235f)
                curveTo(16f, 4.602f, 16f, 5.068f, 16f, 6f)
                moveTo(16f, 6f)
                horizontalLineTo(18f)
                moveTo(18f, 6f)
                horizontalLineTo(20f)
            }
        }.build()

        return _TrashEmpty!!
    }

@Suppress("ObjectPropertyName")
private var _TrashEmpty: ImageVector? = null