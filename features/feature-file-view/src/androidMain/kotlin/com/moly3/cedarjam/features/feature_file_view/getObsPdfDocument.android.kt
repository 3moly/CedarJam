package com.moly3.cedarjam.features.feature_file_view

import androidx.compose.ui.graphics.painter.Painter

actual fun getObsPdfDocument(absolutePath: String): ObsPdfDocument? {
    return object : ObsPdfDocument {
        override fun getNumberOfPages(): Int {
            return 0
        }

        override fun getPagePainter(index: Int): Painter? {
            return null
        }

        override fun getPageText(index: Int): String? {
            return null
        }

        override fun getPdfData(): PdfData? {
            return null
        }
    }
}