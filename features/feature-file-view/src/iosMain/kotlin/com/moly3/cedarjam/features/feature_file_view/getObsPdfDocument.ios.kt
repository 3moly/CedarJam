package com.moly3.cedarjam.features.feature_file_view

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.painter.Painter
import com.moly3.cedarjam.core.domain.io
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
    return remember(loadedDocument) {
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