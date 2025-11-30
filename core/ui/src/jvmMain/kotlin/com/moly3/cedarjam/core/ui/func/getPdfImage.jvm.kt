package com.moly3.cedarjam.core.ui.func

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import org.apache.pdfbox.Loader
import org.apache.pdfbox.rendering.PDFRenderer
import org.jetbrains.skia.Image
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.io.File
import javax.imageio.ImageIO

actual fun getPdfImage(
    path: String,
    page: Int,
    dpi: Float
): ImageBitmap {
    val document = Loader.loadPDF(File(path))
    val renderer = PDFRenderer(document)
    val image: BufferedImage = renderer.renderImageWithDPI(page, dpi)
    return bufferedImageToImageBitmap(image)
}

fun bufferedImageToImageBitmap(bufferedImage: BufferedImage): ImageBitmap {
    val out = ByteArrayOutputStream()
    ImageIO.write(bufferedImage, "png", out)
    val byteArray = out.toByteArray()
    val skiaImage = Image.makeFromEncoded(byteArray)
    return skiaImage.toComposeImageBitmap()
}