package com.moly3.cedarjam.features.feature_file_view

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.painter.Painter
import com.dshatz.pdfmp.compose.state.DisplayState
import com.dshatz.pdfmp.compose.state.rememberPdfState
import com.dshatz.pdfmp.source.PdfSource
import kotlinx.io.files.Path

@Composable
actual fun getObsPdfDocument(absolutePath: String): ObsPdfDocument? {
    val state = rememberPdfState(PdfSource.PdfPath(Path(absolutePath)))
    val layoutInfo by state.layoutInfo()
    val totalPages = layoutInfo?.totalPages?.value
    return object : ObsPdfDocument {
        override fun isActive(): Boolean {
            return state.displayState.value is DisplayState.Active
        }

        override fun getNumberOfPages(): Int {
            return totalPages ?: 0
        }

        override fun getPagePainter(index: Int): Painter? {
            return null
        }

        override fun getPdfData(): PdfData? {
            return null
        }
    }
}