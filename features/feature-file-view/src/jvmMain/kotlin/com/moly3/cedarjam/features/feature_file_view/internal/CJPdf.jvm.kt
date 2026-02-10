package com.moly3.cedarjam.features.feature_file_view.internal

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import com.dshatz.pdfmp.compose.PdfView
import com.dshatz.pdfmp.compose.state.rememberPdfState
import com.dshatz.pdfmp.source.PdfSource
import com.moly3.cedarjam.core.domain.model.AnnotationDTO
import com.moly3.cedarjam.core.domain.model.request.CreateAnnotationRequest
import com.moly3.cedarjam.features.feature_file_view.ObsPdfDocument
import kotlinx.collections.immutable.ImmutableList
import kotlinx.io.files.Path

@Composable
actual fun CJPdf(
    modifier: Modifier,
    currentPage: Int,
    pdf: ObsPdfDocument,
    annotations: ImmutableList<AnnotationDTO>,
    filePath: String,
    onAddAnnotation: (CreateAnnotationRequest) -> Unit,
    onDeleteAnnotation: (AnnotationDTO) -> Unit
) {
    val state = rememberPdfState(PdfSource.PdfPath(Path(filePath)))
    PdfView(state, modifier.fillMaxSize())
    LaunchedEffect(currentPage) {
        try {
//           val staa= state.displayState.value
//
//            if (state.isInitialized.value) {
//                state.listState.scrollToItem(currentPage)
//            }

        } catch (exc: Exception) {
        }
    }
}