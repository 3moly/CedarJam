package vectors

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val Tag: ImageVector
    get() {
        if (_Tag != null) {
            return _Tag!!
        }
        _Tag = ImageVector.Builder(
            name = "Tag",
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
                moveTo(4.748f, 7.645f)
                lineTo(4.417f, 11.288f)
                curveTo(4.368f, 11.828f, 4.343f, 12.1f, 4.388f, 12.358f)
                curveTo(4.428f, 12.589f, 4.508f, 12.811f, 4.625f, 13.014f)
                curveTo(4.756f, 13.242f, 4.95f, 13.436f, 5.336f, 13.822f)
                lineTo(10.512f, 18.998f)
                curveTo(11.299f, 19.785f, 11.693f, 20.179f, 12.148f, 20.326f)
                curveTo(12.55f, 20.457f, 12.983f, 20.457f, 13.385f, 20.327f)
                curveTo(13.841f, 20.178f, 14.238f, 19.782f, 15.03f, 18.99f)
                lineTo(18.99f, 15.03f)
                curveTo(19.782f, 14.238f, 20.177f, 13.842f, 20.326f, 13.386f)
                curveTo(20.456f, 12.984f, 20.455f, 12.551f, 20.325f, 12.149f)
                curveTo(20.177f, 11.693f, 19.782f, 11.297f, 18.99f, 10.505f)
                lineTo(13.827f, 5.342f)
                curveTo(13.438f, 4.953f, 13.243f, 4.758f, 13.014f, 4.626f)
                curveTo(12.811f, 4.509f, 12.589f, 4.429f, 12.358f, 4.388f)
                curveTo(12.097f, 4.343f, 11.823f, 4.368f, 11.274f, 4.418f)
                lineTo(7.644f, 4.748f)
                curveTo(6.7f, 4.834f, 6.227f, 4.877f, 5.857f, 5.083f)
                curveTo(5.531f, 5.264f, 5.263f, 5.532f, 5.082f, 5.858f)
                curveTo(4.877f, 6.226f, 4.834f, 6.696f, 4.749f, 7.631f)
                lineTo(4.748f, 7.645f)
                close()
            }
            path(
                stroke = SolidColor(Color.White),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(9.713f, 9.713f)
                curveTo(10.103f, 9.322f, 10.103f, 8.689f, 9.713f, 8.299f)
                curveTo(9.322f, 7.908f, 8.688f, 7.908f, 8.298f, 8.299f)
                curveTo(7.907f, 8.689f, 7.907f, 9.322f, 8.298f, 9.713f)
                curveTo(8.688f, 10.103f, 9.322f, 10.104f, 9.713f, 9.713f)
                close()
            }
        }.build()

        return _Tag!!
    }

@Suppress("ObjectPropertyName")
private var _Tag: ImageVector? = null