package com.moly3.cedarjam.features.feature_file_view

import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.painter.Painter

@Stable
interface ObsPdfDocument {
    fun getNumberOfPages(): Int
    fun getPagePainter(index: Int): Painter?
    fun getPageText(index: Int): String?
    fun getPdfData(): PdfData?
}

expect class PdfData