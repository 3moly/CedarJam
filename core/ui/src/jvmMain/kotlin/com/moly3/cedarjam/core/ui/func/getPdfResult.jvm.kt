package com.moly3.cedarjam.core.ui.func

import com.moly3.cedarjam.core.ui.model.PdfResult
import org.apache.pdfbox.Loader
import java.io.File

actual suspend fun getPdfResult(path: String): PdfResult {
    val document = Loader.loadPDF(File(path))
    return PdfResult(numberOfPages = document.numberOfPages)
}