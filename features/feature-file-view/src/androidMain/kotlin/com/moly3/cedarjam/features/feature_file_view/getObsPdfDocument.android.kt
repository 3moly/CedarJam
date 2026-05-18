package com.moly3.cedarjam.features.feature_file_view

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.Build
import android.os.ParcelFileDescriptor
import androidx.annotation.RequiresApi
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import java.io.File

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
@Composable
actual fun getObsPdfDocument(absolutePath: String): ObsPdfDocument? {
    val context = androidx.compose.ui.platform.LocalContext.current

    val rendererHolder = remember(absolutePath) {
        try {
            val file = File(absolutePath)
            val pfd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
            val renderer = PdfRenderer(pfd)
            AndroidPdfDocument(renderer, pfd)
        } catch (e: Exception) {
            null
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            rendererHolder?.close()
        }
    }

    return rendererHolder
}

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
private class AndroidPdfDocument(
    private val renderer: PdfRenderer,
    private val fileDescriptor: ParcelFileDescriptor,

) : ObsPdfDocument {

    override fun isActive(): Boolean = true

    override fun getNumberOfPages(): Int = renderer.pageCount

    override val pdfDataState: State<PdfData?> = derivedStateOf { null }


    override fun getPagePainter(index: Int): Painter? {
        if (index !in 0 until renderer.pageCount) return null

        val page = renderer.openPage(index)

        val width = page.width * 2
        val height = page.height * 2

        val bitmap = Bitmap.createBitmap(
            width,
            height,
            Bitmap.Config.ARGB_8888
        )
        bitmap.eraseColor(android.graphics.Color.WHITE)
        page.render(
            bitmap,
            null,
            null,
            PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY
        )

        page.close()

        val imageBitmap: ImageBitmap = bitmap.asImageBitmap()
        return BitmapPainter(imageBitmap)
    }

    override fun getPdfData(): PdfData? = null

    fun close() {
        renderer.close()
        fileDescriptor.close()
    }
}