package vectors

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val ChevronDownDuo: ImageVector
    get() {
        if (_ChevronDownDuo != null) {
            return _ChevronDownDuo!!
        }
        _ChevronDownDuo = ImageVector.Builder(
            name = "ChevronDownDuo",
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
                moveTo(16f, 13f)
                lineTo(12f, 17f)
                lineTo(8f, 13f)
                moveTo(16f, 7f)
                lineTo(12f, 11f)
                lineTo(8f, 7f)
            }
        }.build()

        return _ChevronDownDuo!!
    }

@Suppress("ObjectPropertyName")
private var _ChevronDownDuo: ImageVector? = null