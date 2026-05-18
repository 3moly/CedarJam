package vector.collection

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val File05: ImageVector
    get() {
        if (_File05 != null) {
            return _File05!!
        }
        _File05 = ImageVector.Builder(
            name = "File05",
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
                moveTo(15f, 2.4f)
                verticalLineTo(6f)
                curveTo(15f, 6.663f, 15.537f, 7.2f, 16.2f, 7.2f)
                horizontalLineTo(19.8f)
                moveTo(8.4f, 7.2f)
                horizontalLineTo(10.8f)
                moveTo(8.4f, 10.8f)
                horizontalLineTo(15.6f)
                moveTo(8.4f, 14.4f)
                horizontalLineTo(15.6f)
                moveTo(18f, 4.2f)
                curveTo(17.466f, 3.722f, 16.912f, 3.155f, 16.562f, 2.787f)
                curveTo(16.329f, 2.542f, 16.007f, 2.4f, 15.67f, 2.4f)
                horizontalLineTo(6.6f)
                curveTo(5.274f, 2.4f, 4.2f, 3.474f, 4.2f, 4.8f)
                lineTo(4.2f, 19.2f)
                curveTo(4.2f, 20.525f, 5.274f, 21.6f, 6.6f, 21.6f)
                lineTo(17.4f, 21.6f)
                curveTo(18.725f, 21.6f, 19.8f, 20.525f, 19.8f, 19.2f)
                lineTo(19.8f, 6.478f)
                curveTo(19.8f, 6.171f, 19.683f, 5.876f, 19.47f, 5.655f)
                curveTo(19.076f, 5.247f, 18.419f, 4.574f, 18f, 4.2f)
                close()
            }
        }.build()

        return _File05!!
    }

@Suppress("ObjectPropertyName")
private var _File05: ImageVector? = null
