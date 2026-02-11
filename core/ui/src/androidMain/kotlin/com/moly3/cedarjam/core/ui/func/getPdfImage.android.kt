package com.moly3.cedarjam.core.ui.func

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.Build
import android.os.ParcelFileDescriptor
import androidx.annotation.RequiresApi
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import java.io.File

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
actual fun getPdfImage(
    path: String,
    page: Int,
    dpi: Float
): ImageBitmap {
    val file = File(path)
    val fileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
    val pdfRenderer = PdfRenderer(fileDescriptor)

    val pdfPage = pdfRenderer.openPage(page)

    // Calculate dimensions based on DPI
    // PdfRenderer uses points (72 DPI). Scale to desired DPI.
    val scale = dpi / 72f
    val width = (pdfPage.width * scale).toInt()
    val height = (pdfPage.height * scale).toInt()

    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    pdfPage.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)

    pdfPage.close()
    pdfRenderer.close()
    fileDescriptor.close()

    return bitmap.asImageBitmap()
}