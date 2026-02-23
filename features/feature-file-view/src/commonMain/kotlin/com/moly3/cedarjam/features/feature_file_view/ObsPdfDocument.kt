package com.moly3.cedarjam.features.feature_file_view

import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.ui.graphics.painter.Painter

@Stable
interface ObsPdfDocument {
    fun isActive(): Boolean
    fun getNumberOfPages(): Int
    fun getPagePainter(index: Int): Painter?
    fun getPdfData(): PdfData?
    val pdfDataState: State<PdfData?>  // ← observable state
}

expect class PdfData