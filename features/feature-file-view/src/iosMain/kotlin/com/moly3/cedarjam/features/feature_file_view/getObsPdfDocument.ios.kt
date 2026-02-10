package com.moly3.cedarjam.features.feature_file_view

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.painter.Painter
import platform.Foundation.NSURL
import platform.PDFKit.PDFDocument

@Composable
actual fun getObsPdfDocument(absolutePath: String): ObsPdfDocument? {
    val loadedDocument = remember(absolutePath) {
        try {
            PDFDocument(NSURL.fileURLWithPath(absolutePath))
        } catch (exc: Exception) {
            null
        }
    }
    return remember {
        object : ObsPdfDocument {
            val documentOwning: PDFDocument? = loadedDocument
            override fun isActive(): Boolean {
                return true
            }

            override fun getNumberOfPages(): Int {
                return documentOwning?.pageCount?.toInt() ?: 1
            }

            override fun getPagePainter(index: Int): Painter? {
                return null
            }

            override fun getPdfData(): PdfData? {
                return documentOwning ?: null
            }
        }
    }
}