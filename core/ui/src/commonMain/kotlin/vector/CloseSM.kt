package vector
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val CloseSM: ImageVector
    get() {
        if (_CloseSM != null) {
            return _CloseSM!!
        }
        _CloseSM = ImageVector.Builder(
            name = "CloseSM",
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
                moveTo(16f, 16f)
                lineTo(12f, 12f)
                moveTo(12f, 12f)
                lineTo(8f, 8f)
                moveTo(12f, 12f)
                lineTo(16f, 8f)
                moveTo(12f, 12f)
                lineTo(8f, 16f)
            }
        }.build()

        return _CloseSM!!
    }

@Suppress("ObjectPropertyName")
private var _CloseSM: ImageVector? = null