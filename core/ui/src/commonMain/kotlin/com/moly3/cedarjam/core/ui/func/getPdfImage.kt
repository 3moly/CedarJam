package com.moly3.cedarjam.core.ui.func

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.ImageBitmapConfig
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.IntSize
import com.moly3.cedarjam.core.domain.io
import com.moly3.cedarjam.core.ui.compositions.LocalWorkspacePath
import kotlinx.coroutines.launch
import kotlinx.io.files.Path

expect fun getPdfImage(
    path: String,
    page: Int,
    density: Float
): ImageBitmap

@Composable
fun rememberPdfBitmap(fileRelativePath: String?): ImageBitmap? {
    var imgBitmap by remember {
        mutableStateOf<ImageBitmap?>(null)
    }
    if (LocalInspectionMode.current) {
        imgBitmap = generatePreviewBitmap()
    }
    val workspacePath = LocalWorkspacePath.current
    val density = LocalDensity.current.density
    LaunchedEffect(fileRelativePath, workspacePath, density) {
        launch(io) {
            try {
                imgBitmap = if (fileRelativePath != null) {
                    getPdfImage(
                        Path(
                            workspacePath,
                            fileRelativePath
                        ).toString(),
                        page = 0,
                        density = density
                    )
                } else {
                    null
                }
            } catch (exc: Exception) {
                imgBitmap = null
            }
        }
    }
    return imgBitmap
}


fun generatePreviewBitmap(
    width: Int = 300,
    height: Int = 420
): ImageBitmap {

    val bitmap = ImageBitmap(width, height)
    val canvas = Canvas(bitmap)

    val paint1 = Paint().apply { color = Color(0xFFE0E0E0) }
    val paint2 = Paint().apply { color = Color(0xFFBDBDBD) }

    val cell = 24

    for (y in 0 until height step cell) {
        for (x in 0 until width step cell) {
            val paint = if ((x / cell + y / cell) % 2 == 0) paint1 else paint2
            canvas.drawRect(
                Rect(
                    left = x.toFloat(),
                    top = y.toFloat(),
                    right = (x + cell).toFloat(),
                    bottom = (y + cell).toFloat()
                ),
                paint
            )
        }
    }

    return bitmap
}

fun ImageBitmap.cropNormalized(
    x: Float,
    y: Float,
    width: Float,
    height: Float
): ImageBitmap {
    val srcWidth = this.width
    val srcHeight = this.height

    val left = (x * srcWidth).toInt()
    val top = (y * srcHeight).toInt()
    val cropWidth = (width * srcWidth).toInt()
    val cropHeight = (height * srcHeight).toInt()

    val result = ImageBitmap(
        width = cropWidth,
        height = cropHeight,
        config = ImageBitmapConfig.Argb8888
    )

    val canvas = Canvas(result)
    canvas.drawRect(
        rect = androidx.compose.ui.geometry.Rect(
            0f,
            0f,
            cropWidth.toFloat(),
            cropHeight.toFloat()
        ),
        paint = Paint().apply {
            color = androidx.compose.ui.graphics.Color.White
        }
    )
    canvas.drawImageRect(
        image = this,
        srcOffset = androidx.compose.ui.unit.IntOffset(left, top),
        srcSize = IntSize(cropWidth, cropHeight),
        dstOffset = androidx.compose.ui.unit.IntOffset.Zero,
        dstSize = IntSize(cropWidth, cropHeight),
        paint = Paint()
    )

    return result
}