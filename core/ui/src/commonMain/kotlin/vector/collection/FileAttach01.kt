package vector.collection

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val FileAttach01: ImageVector
    get() {
        if (_FileAttach01 != null) {
            return _FileAttach01!!
        }
        _FileAttach01 = ImageVector.Builder(
            name = "FileAttach01",
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
                moveTo(11.7f, 21.6f)
                horizontalLineTo(5.7f)
                curveTo(4.374f, 21.6f, 3.3f, 20.526f, 3.3f, 19.2f)
                lineTo(3.3f, 4.8f)
                curveTo(3.3f, 3.475f, 4.374f, 2.4f, 5.7f, 2.4f)
                horizontalLineTo(16.5f)
                curveTo(17.826f, 2.4f, 18.9f, 3.475f, 18.9f, 4.8f)
                verticalLineTo(9.6f)
                moveTo(7.5f, 7.2f)
                horizontalLineTo(14.7f)
                moveTo(7.5f, 10.8f)
                horizontalLineTo(14.7f)
                moveTo(14.7f, 15.554f)
                verticalLineTo(18.499f)
                curveTo(14.7f, 19.953f, 16.252f, 21.288f, 17.706f, 21.288f)
                curveTo(19.162f, 21.288f, 20.7f, 19.954f, 20.7f, 18.499f)
                verticalLineTo(14.779f)
                curveTo(20.7f, 14.009f, 20.257f, 13.227f, 19.272f, 13.227f)
                curveTo(18.219f, 13.227f, 17.706f, 14.009f, 17.706f, 14.779f)
                verticalLineTo(18.344f)
                moveTo(7.5f, 14.4f)
                horizontalLineTo(11.1f)
            }
        }.build()

        return _FileAttach01!!
    }

@Suppress("ObjectPropertyName")
private var _FileAttach01: ImageVector? = null
