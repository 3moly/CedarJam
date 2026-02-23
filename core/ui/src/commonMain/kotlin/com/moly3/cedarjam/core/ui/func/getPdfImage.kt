package com.moly3.cedarjam.core.ui.func

import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.ImageBitmapConfig
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.unit.IntSize

expect fun getPdfImage(
    path: String,
    page: Int,
    density: Float
): ImageBitmap

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