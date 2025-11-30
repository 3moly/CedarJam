package com.moly3.cedarjam.core.ui.func

import com.moly3.cedarjam.core.ui.model.PdfResult

expect suspend fun getPdfResult(
    path: String
): PdfResult