package com.moly3.cedarjam.features.feature_file_view.internal

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.moly3.cedarjam.core.domain.model.AnnotationDTO
import com.moly3.cedarjam.core.domain.model.request.CreateAnnotationRequest
import com.moly3.cedarjam.features.feature_file_view.ObsPdfDocument
import kotlinx.collections.immutable.ImmutableList

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
//    val state = rememberPdfState(PdfSource.PdfPath(Path(filePath)))
//    val layoutInfo by state.layoutInfo()
//    PdfView(state, modifier.fillMaxSize())
//    LaunchedEffect(currentPage,layoutInfo) {
//        try {
//            layoutInfo?.apply {
//                this.scrollTo(currentPage)
////                visiblePages.apply {
////                    value = listOf()
////                }
//            }
//            //           val staa= state.displayState.value
////
////            if (state.isInitialized.value) {
////                state.listState.scrollToItem(currentPage)
////            }
//
//        } catch (exc: Exception) {
//        }
//    }
}