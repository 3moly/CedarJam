package com.moly3.cedarjam.features.feature_file_view

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalDensity
import com.moly3.cedarjam.core.domain.io
import com.moly3.cedarjam.core.ui.func.getPdfImage
import kotlinx.coroutines.launch
import platform.Foundation.NSURL
import platform.PDFKit.PDFDocument

@Composable
actual fun getObsPdfDocument(absolutePath: String): ObsPdfDocument? {
    var loadedDocument by remember(absolutePath) {
        mutableStateOf<PDFDocument?>(null)
    }
    val scope = rememberCoroutineScope()
    LaunchedEffect(absolutePath) {
        scope.launch(io) {
            loadedDocument = try {
                PDFDocument(NSURL.fileURLWithPath(absolutePath))
            } catch (exc: Exception) {
                null
            }
        }
    }
    val density = LocalDensity.current.density
    return remember(loadedDocument, density) {
        object : ObsPdfDocument {
            val documentOwning: PDFDocument? = loadedDocument
            override fun isActive(): Boolean {
                return true
            }

            override val pdfDataState: State<PdfData?> = derivedStateOf {
                loadedDocument
            }

            override fun getNumberOfPages(): Int {
                return documentOwning?.pageCount?.toInt() ?: 1
            }

            override fun getPagePainter(index: Int): Painter? {
                val bitmap = getPdfImage(absolutePath, index, density)
                return BitmapPainter(bitmap)
            }

            override fun getPdfData(): PdfData? {
                return documentOwning ?: null
            }
        }
    }
}