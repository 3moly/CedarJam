package com.moly3.cedarjam.core.ui.func

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asComposeImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.refTo
import platform.CoreGraphics.*
import platform.Foundation.*
import platform.PDFKit.*
import platform.UIKit.*
import org.jetbrains.skia.Image
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.Canvas
import org.jetbrains.skia.ColorAlphaType
import org.jetbrains.skia.ColorType
import org.jetbrains.skia.ImageInfo

@OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)
actual fun getPdfImage(
    path: String,
    page: Int,
    density: Float
): ImageBitmap {
    val url = NSURL.fileURLWithPath(path)
    val document = PDFDocument(url) ?: throw IllegalArgumentException("Could not load PDF at $path")
    val pdfPage = document.pageAtIndex(page.toULong()) ?: throw IllegalArgumentException("Page $page not found")

    // PDFKit coordinates are points (1/72 inch). Calculate scale based on desired DPI.
    val scale = (density * 160f) / 72f
    val pageRect = pdfPage.boundsForBox(kPDFDisplayBoxMediaBox)
    val pointWidth = CGRectGetWidth(pageRect)
    val pointHeight = CGRectGetHeight(pageRect)

    val rendererFormat = UIGraphicsImageRendererFormat.defaultFormat().apply {
        this.scale = scale.toDouble()
    }
    val renderer = UIGraphicsImageRenderer(
        size = CGSizeMake(pointWidth, pointHeight),
        format = rendererFormat
    )

    val uiImage = renderer.imageWithActions { _ ->
        val cgContext = UIGraphicsGetCurrentContext()
        if (cgContext != null) {
            CGContextTranslateCTM(cgContext, 0.0, pointHeight)
            CGContextScaleCTM(cgContext, 1.0, -1.0)  // No DPI scale here — renderer handles it
            pdfPage.drawWithBox(kPDFDisplayBoxMediaBox, toContext = cgContext)
        }
    }

    // Convert UIImage to Skia Image for Compose
    val data = UIImagePNGRepresentation(uiImage) ?: throw Exception("Failed to encode PDF page to PNG")
    val bytes = data.bytes?.let { NSData.dataWithBytes(it, data.length).toByteArray() } ?: throw Exception("Data conversion failed")
    val skiaImage = Image.makeFromEncoded(bytes)
    return skiaImage.toComposeImageBitmap()
}


// Helper to convert NSData to ByteArray
@OptIn(ExperimentalForeignApi::class)
private fun NSData.toByteArray(): ByteArray {
    val size = length.toInt()
    val byteArray = ByteArray(size)
    if (size > 0) {
        val pointer = bytes
        platform.posix.memcpy(byteArray.refTo(0), pointer, size.toULong())
    }
    return byteArray
}