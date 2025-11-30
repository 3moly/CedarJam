package com.moly3.cedarjam.features.feature_file_view

import androidx.compose.ui.graphics.painter.Painter

interface ObsPdfDocument {
    fun getNumberOfPages(): Int
    fun getPagePainter(index: Int): Painter?
    fun getPageText(index: Int): String?
}