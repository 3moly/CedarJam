package vector

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val Bulb: ImageVector
    get() {
        if (_Bulb != null) {
            return _Bulb!!
        }
        _Bulb = ImageVector.Builder(
            name = "Bulb",
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
                moveTo(9f, 21f)
                horizontalLineTo(15f)
                moveTo(12f, 3f)
                curveTo(8.686f, 3f, 6f, 5.686f, 6f, 9f)
                curveTo(6f, 10.215f, 6.361f, 11.345f, 6.981f, 12.289f)
                curveTo(7.935f, 13.742f, 8.412f, 14.468f, 8.474f, 14.576f)
                curveTo(9.024f, 15.541f, 8.923f, 15.201f, 8.992f, 16.31f)
                curveTo(9f, 16.434f, 9f, 16.623f, 9f, 17f)
                curveTo(9f, 17.552f, 9.448f, 18f, 10f, 18f)
                lineTo(14f, 18f)
                curveTo(14.552f, 18f, 15f, 17.552f, 15f, 17f)
                curveTo(15f, 16.623f, 15f, 16.434f, 15.008f, 16.31f)
                curveTo(15.077f, 15.201f, 14.975f, 15.541f, 15.526f, 14.576f)
                curveTo(15.588f, 14.468f, 16.065f, 13.742f, 17.019f, 12.289f)
                curveTo(17.639f, 11.345f, 18f, 10.215f, 18f, 9f)
                curveTo(18f, 5.686f, 15.314f, 3f, 12f, 3f)
                close()
            }
        }.build()

        return _Bulb!!
    }

@Suppress("ObjectPropertyName")
private var _Bulb: ImageVector? = null
