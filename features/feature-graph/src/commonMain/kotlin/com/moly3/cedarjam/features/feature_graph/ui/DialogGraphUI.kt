package com.moly3.cedarjam.features.feature_graph.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import com.moly3.cedarjam.core.ui.model.PageNameData
import com.moly3.cedarjam.features.feature_graph.IDialogGraphComponent
import com.moly3.cedarjam.features.feature_graph.ui.internal.DialogGraphUIContent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun DialogGraphUI(
    pageNameData: PageNameData?,
    component: IDialogGraphComponent
) {
    val scope = rememberCoroutineScope()
    val state by component.state.collectAsState()

    DialogGraphUIContent(
        state = state,
        pageNameData = pageNameData,
        onIntent = {
            scope.launch(Dispatchers.Main) {
                component.onIntent(it)
            }
        }
    )
}