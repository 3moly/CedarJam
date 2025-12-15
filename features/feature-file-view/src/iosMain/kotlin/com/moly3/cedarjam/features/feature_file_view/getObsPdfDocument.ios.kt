package com.moly3.cedarjam.features.feature_file_view

import androidx.compose.ui.graphics.painter.Painter
import platform.Foundation.NSURL
import platform.PDFKit.PDFDocument

actual fun getObsPdfDocument(absolutePath: String): ObsPdfDocument? {
    val loadedDocument = try {
        PDFDocument(NSURL.fileURLWithPath(absolutePath))
    } catch (exc: Exception) {
        null
    }
    return object : ObsPdfDocument {
        val documentOwning: PDFDocument? = loadedDocument

        override fun getNumberOfPages(): Int {
            return documentOwning?.pageCount?.toInt() ?: 1
        }

        override fun getPagePainter(index: Int): Painter? {
            return null
        }

        override fun getPageText(index: Int): String? {
            return null
        }

        override fun getPdfData(): PdfData? {
            return documentOwning ?: null
        }
    }
}