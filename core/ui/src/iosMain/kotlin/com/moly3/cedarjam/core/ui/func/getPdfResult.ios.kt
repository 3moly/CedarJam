package com.moly3.cedarjam.core.ui.func

import com.moly3.cedarjam.core.ui.model.PdfResult
import platform.Foundation.NSURL
import platform.PDFKit.PDFDocument

actual suspend fun getPdfResult(path: String): PdfResult {
    val url = NSURL.fileURLWithPath(path)
    val document = PDFDocument(url)
    return PdfResult(numberOfPages = document.pageCount.toInt())
}