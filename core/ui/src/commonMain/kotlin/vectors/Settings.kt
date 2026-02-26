package vectors

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val Settings: ImageVector
    get() {
        if (_Settings != null) {
            return _Settings!!
        }
        _Settings = ImageVector.Builder(
            name = "Settings",
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
                moveTo(20.35f, 8.923f)
                lineTo(19.984f, 8.719f)
                curveTo(19.927f, 8.688f, 19.899f, 8.672f, 19.871f, 8.655f)
                curveTo(19.598f, 8.492f, 19.368f, 8.266f, 19.2f, 7.995f)
                curveTo(19.183f, 7.968f, 19.167f, 7.939f, 19.135f, 7.883f)
                curveTo(19.102f, 7.827f, 19.086f, 7.798f, 19.071f, 7.77f)
                curveTo(18.92f, 7.489f, 18.838f, 7.175f, 18.834f, 6.856f)
                curveTo(18.833f, 6.824f, 18.833f, 6.791f, 18.834f, 6.726f)
                lineTo(18.841f, 6.301f)
                curveTo(18.853f, 5.62f, 18.859f, 5.279f, 18.763f, 4.973f)
                curveTo(18.678f, 4.701f, 18.536f, 4.45f, 18.346f, 4.237f)
                curveTo(18.132f, 3.997f, 17.835f, 3.825f, 17.24f, 3.483f)
                lineTo(16.746f, 3.198f)
                curveTo(16.154f, 2.857f, 15.857f, 2.686f, 15.542f, 2.621f)
                curveTo(15.264f, 2.563f, 14.976f, 2.566f, 14.699f, 2.628f)
                curveTo(14.386f, 2.698f, 14.093f, 2.874f, 13.508f, 3.224f)
                lineTo(13.505f, 3.226f)
                lineTo(13.151f, 3.437f)
                curveTo(13.095f, 3.471f, 13.066f, 3.488f, 13.038f, 3.503f)
                curveTo(12.76f, 3.658f, 12.45f, 3.744f, 12.131f, 3.754f)
                curveTo(12.099f, 3.755f, 12.066f, 3.755f, 12.001f, 3.755f)
                curveTo(11.936f, 3.755f, 11.902f, 3.755f, 11.87f, 3.754f)
                curveTo(11.552f, 3.744f, 11.24f, 3.658f, 10.962f, 3.502f)
                curveTo(10.933f, 3.487f, 10.906f, 3.47f, 10.85f, 3.436f)
                lineTo(10.493f, 3.222f)
                curveTo(9.904f, 2.868f, 9.609f, 2.691f, 9.294f, 2.621f)
                curveTo(9.016f, 2.558f, 8.727f, 2.556f, 8.448f, 2.615f)
                curveTo(8.132f, 2.681f, 7.836f, 2.853f, 7.243f, 3.197f)
                lineTo(7.24f, 3.198f)
                lineTo(6.752f, 3.481f)
                lineTo(6.747f, 3.485f)
                curveTo(6.159f, 3.826f, 5.864f, 3.997f, 5.652f, 4.236f)
                curveTo(5.463f, 4.449f, 5.322f, 4.699f, 5.237f, 4.97f)
                curveTo(5.142f, 5.277f, 5.147f, 5.619f, 5.159f, 6.303f)
                lineTo(5.166f, 6.727f)
                curveTo(5.167f, 6.792f, 5.169f, 6.824f, 5.168f, 6.855f)
                curveTo(5.163f, 7.175f, 5.081f, 7.489f, 4.93f, 7.771f)
                curveTo(4.915f, 7.799f, 4.899f, 7.827f, 4.867f, 7.882f)
                curveTo(4.834f, 7.938f, 4.819f, 7.966f, 4.802f, 7.993f)
                curveTo(4.633f, 8.265f, 4.402f, 8.492f, 4.127f, 8.656f)
                curveTo(4.1f, 8.672f, 4.071f, 8.688f, 4.015f, 8.719f)
                lineTo(3.654f, 8.919f)
                curveTo(3.052f, 9.252f, 2.751f, 9.419f, 2.533f, 9.657f)
                curveTo(2.339f, 9.867f, 2.193f, 10.116f, 2.103f, 10.387f)
                curveTo(2.003f, 10.694f, 2.003f, 11.038f, 2.004f, 11.726f)
                lineTo(2.006f, 12.288f)
                curveTo(2.007f, 12.971f, 2.009f, 13.312f, 2.11f, 13.617f)
                curveTo(2.2f, 13.886f, 2.345f, 14.134f, 2.537f, 14.343f)
                curveTo(2.755f, 14.579f, 3.053f, 14.745f, 3.65f, 15.077f)
                lineTo(4.008f, 15.276f)
                curveTo(4.069f, 15.31f, 4.1f, 15.327f, 4.129f, 15.344f)
                curveTo(4.401f, 15.508f, 4.631f, 15.735f, 4.798f, 16.005f)
                curveTo(4.816f, 16.035f, 4.834f, 16.065f, 4.868f, 16.125f)
                curveTo(4.903f, 16.185f, 4.92f, 16.215f, 4.936f, 16.245f)
                curveTo(5.083f, 16.523f, 5.161f, 16.831f, 5.166f, 17.146f)
                curveTo(5.167f, 17.179f, 5.167f, 17.214f, 5.165f, 17.283f)
                lineTo(5.159f, 17.69f)
                curveTo(5.147f, 18.376f, 5.142f, 18.72f, 5.238f, 19.027f)
                curveTo(5.323f, 19.299f, 5.465f, 19.55f, 5.655f, 19.763f)
                curveTo(5.869f, 20.003f, 6.167f, 20.174f, 6.761f, 20.517f)
                lineTo(7.255f, 20.802f)
                curveTo(7.848f, 21.143f, 8.144f, 21.314f, 8.459f, 21.379f)
                curveTo(8.737f, 21.437f, 9.025f, 21.434f, 9.302f, 21.372f)
                curveTo(9.616f, 21.302f, 9.909f, 21.126f, 10.496f, 20.774f)
                lineTo(10.85f, 20.563f)
                curveTo(10.906f, 20.529f, 10.935f, 20.512f, 10.963f, 20.497f)
                curveTo(11.241f, 20.342f, 11.551f, 20.256f, 11.87f, 20.246f)
                curveTo(11.901f, 20.245f, 11.934f, 20.245f, 11.999f, 20.245f)
                curveTo(12.065f, 20.245f, 12.097f, 20.245f, 12.13f, 20.246f)
                curveTo(12.448f, 20.256f, 12.761f, 20.342f, 13.039f, 20.497f)
                curveTo(13.064f, 20.511f, 13.089f, 20.526f, 13.132f, 20.552f)
                lineTo(13.508f, 20.778f)
                curveTo(14.097f, 21.132f, 14.392f, 21.308f, 14.707f, 21.379f)
                curveTo(14.985f, 21.441f, 15.274f, 21.444f, 15.553f, 21.385f)
                curveTo(15.868f, 21.32f, 16.166f, 21.147f, 16.759f, 20.803f)
                lineTo(17.254f, 20.516f)
                curveTo(17.842f, 20.174f, 18.137f, 20.003f, 18.35f, 19.764f)
                curveTo(18.538f, 19.551f, 18.68f, 19.301f, 18.764f, 19.03f)
                curveTo(18.859f, 18.725f, 18.853f, 18.386f, 18.842f, 17.712f)
                lineTo(18.834f, 17.272f)
                curveTo(18.833f, 17.208f, 18.833f, 17.176f, 18.834f, 17.145f)
                curveTo(18.838f, 16.825f, 18.92f, 16.51f, 19.071f, 16.229f)
                curveTo(19.086f, 16.201f, 19.102f, 16.173f, 19.134f, 16.117f)
                curveTo(19.166f, 16.062f, 19.183f, 16.034f, 19.199f, 16.007f)
                curveTo(19.368f, 15.735f, 19.6f, 15.507f, 19.874f, 15.344f)
                curveTo(19.901f, 15.328f, 19.929f, 15.312f, 19.984f, 15.282f)
                lineTo(19.986f, 15.281f)
                lineTo(20.347f, 15.08f)
                curveTo(20.949f, 14.747f, 21.25f, 14.58f, 21.469f, 14.343f)
                curveTo(21.663f, 14.133f, 21.809f, 13.884f, 21.898f, 13.613f)
                curveTo(21.998f, 13.308f, 21.997f, 12.966f, 21.996f, 12.286f)
                lineTo(21.994f, 11.712f)
                curveTo(21.993f, 11.029f, 21.992f, 10.687f, 21.891f, 10.383f)
                curveTo(21.802f, 10.113f, 21.656f, 9.866f, 21.463f, 9.657f)
                curveTo(21.246f, 9.421f, 20.948f, 9.255f, 20.352f, 8.924f)
                lineTo(20.35f, 8.923f)
                close()
            }
            path(
                stroke = SolidColor(Color.White),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(8f, 12f)
                curveTo(8f, 14.209f, 9.791f, 16f, 12f, 16f)
                curveTo(14.21f, 16f, 16f, 14.209f, 16f, 12f)
                curveTo(16f, 9.791f, 14.21f, 8f, 12f, 8f)
                curveTo(9.791f, 8f, 8f, 9.791f, 8f, 12f)
                close()
            }
        }.build()

        return _Settings!!
    }

@Suppress("ObjectPropertyName")
private var _Settings: ImageVector? = null
