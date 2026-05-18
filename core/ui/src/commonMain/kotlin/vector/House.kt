package vector

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val House: ImageVector
    get() {
        if (_House != null) {
            return _House!!
        }
        _House = ImageVector.Builder(
            name = "House",
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
                moveTo(20f, 17f)
                verticalLineTo(11.452f)
                curveTo(20f, 10.918f, 20f, 10.651f, 19.935f, 10.402f)
                curveTo(19.877f, 10.182f, 19.782f, 9.973f, 19.655f, 9.785f)
                curveTo(19.51f, 9.572f, 19.31f, 9.396f, 18.907f, 9.044f)
                lineTo(14.107f, 4.844f)
                curveTo(13.361f, 4.191f, 12.988f, 3.864f, 12.567f, 3.74f)
                curveTo(12.197f, 3.63f, 11.803f, 3.63f, 11.432f, 3.74f)
                curveTo(11.013f, 3.864f, 10.64f, 4.19f, 9.894f, 4.842f)
                lineTo(5.093f, 9.044f)
                curveTo(4.691f, 9.396f, 4.49f, 9.572f, 4.346f, 9.785f)
                curveTo(4.218f, 9.973f, 4.123f, 10.182f, 4.065f, 10.402f)
                curveTo(4f, 10.651f, 4f, 10.918f, 4f, 11.452f)
                verticalLineTo(17f)
                curveTo(4f, 17.932f, 4f, 18.398f, 4.152f, 18.765f)
                curveTo(4.355f, 19.255f, 4.744f, 19.645f, 5.234f, 19.848f)
                curveTo(5.602f, 20f, 6.068f, 20f, 7f, 20f)
                curveTo(7.932f, 20f, 8.398f, 20f, 8.766f, 19.848f)
                curveTo(9.256f, 19.645f, 9.645f, 19.256f, 9.848f, 18.765f)
                curveTo(10f, 18.398f, 10f, 17.932f, 10f, 17f)
                verticalLineTo(16f)
                curveTo(10f, 14.896f, 10.895f, 14f, 12f, 14f)
                curveTo(13.105f, 14f, 14f, 14.896f, 14f, 16f)
                verticalLineTo(17f)
                curveTo(14f, 17.932f, 14f, 18.398f, 14.152f, 18.765f)
                curveTo(14.355f, 19.256f, 14.744f, 19.645f, 15.234f, 19.848f)
                curveTo(15.602f, 20f, 16.068f, 20f, 17f, 20f)
                curveTo(17.932f, 20f, 18.398f, 20f, 18.766f, 19.848f)
                curveTo(19.256f, 19.645f, 19.645f, 19.255f, 19.848f, 18.765f)
                curveTo(20f, 18.398f, 20f, 17.932f, 20f, 17f)
                close()
            }
        }.build()

        return _House!!
    }

@Suppress("ObjectPropertyName")
private var _House: ImageVector? = null