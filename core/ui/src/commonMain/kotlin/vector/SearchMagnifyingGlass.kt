package vector

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val SearchMagnifyingGlass: ImageVector
    get() {
        if (_SearchMagnifyingGlass != null) {
            return _SearchMagnifyingGlass!!
        }
        _SearchMagnifyingGlass = ImageVector.Builder(
            name = "SearchMagnifyingGlass",
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
                moveTo(15f, 15f)
                lineTo(21f, 21f)
                moveTo(10f, 17f)
                curveTo(6.134f, 17f, 3f, 13.866f, 3f, 10f)
                curveTo(3f, 6.134f, 6.134f, 3f, 10f, 3f)
                curveTo(13.866f, 3f, 17f, 6.134f, 17f, 10f)
                curveTo(17f, 13.866f, 13.866f, 17f, 10f, 17f)
                close()
            }
        }.build()

        return _SearchMagnifyingGlass!!
    }

@Suppress("ObjectPropertyName")
private var _SearchMagnifyingGlass: ImageVector? = null