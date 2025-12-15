package com.moly3.cedarjam.features.feature_file_view.internal

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import com.moly3.cedarjam.features.feature_file_view.ObsPdfDocument

@Stable
data class PdfBytes(
    val byteArray: ByteArray
)

@Composable
expect fun CJPdf(
    modifier: Modifier,
    currentPage: Int,
    pdf: ObsPdfDocument,
    filePath: String,
)