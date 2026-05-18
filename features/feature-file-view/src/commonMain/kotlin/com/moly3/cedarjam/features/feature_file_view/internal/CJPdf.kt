package com.moly3.cedarjam.features.feature_file_view.internal

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.moly3.cedarjam.core.domain.model.AnnotationDTO
import com.moly3.cedarjam.core.domain.model.request.CreateAnnotationRequest
import com.moly3.cedarjam.features.feature_file_view.ObsPdfDocument
import kotlinx.collections.immutable.ImmutableList

@Composable
expect fun CJPdf(
    modifier: Modifier,
    currentPage: Int,
    pdf: ObsPdfDocument,
    annotations: ImmutableList<AnnotationDTO>,
    filePath: String,
    onAddAnnotation: (CreateAnnotationRequest) -> Unit,
    onDeleteAnnotation: (AnnotationDTO) -> Unit
)