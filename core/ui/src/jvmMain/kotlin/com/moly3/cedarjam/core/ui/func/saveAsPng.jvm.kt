package com.moly3.cedarjam.core.ui.func

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toAwtImage
import java.io.File
import javax.imageio.ImageIO

actual suspend fun ImageBitmap.saveAsPng(path: String) {
    val awtImage = this.toAwtImage()
    val file = File(path)
    if (!file.exists()) {
        file.parentFile.mkdirs()
    }
    ImageIO.write(awtImage, "png", file)
}