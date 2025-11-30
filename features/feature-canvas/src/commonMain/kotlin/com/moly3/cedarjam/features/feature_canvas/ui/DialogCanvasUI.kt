package com.moly3.cedarjam.features.feature_canvas.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.moly3.cedarjam.core.domain.model.FileType
import com.moly3.cedarjam.features.feature_canvas.IDialogCanvasComponent
import com.moly3.cedarjam.features.feature_canvas.ui.internal.DialogCanvasUIContent

@Composable
fun DialogCanvasUI(
    modifier: Modifier,
    component: IDialogCanvasComponent,
    onFileTypeView: @Composable (FileType) -> Unit,
) {
    val state by component.state.collectAsState()
    DialogCanvasUIContent(
        modifier = modifier,
        state = state,
        filesRepository = component.filesRepository,
        onFileTypeView = onFileTypeView,
        onIntent = {
            component.onIntent(it)
        }
    )
}