package com.moly3.cedarjam.ui.pages.tag

import androidx.compose.runtime.Immutable
import com.moly3.cedarjam.core.ui.model.PageNameData
import com.moly3.cedarjam.features.feature_graph.IDialogGraphContainer
import kotlinx.coroutines.flow.StateFlow

@Immutable
interface TagComponent : IDialogGraphContainer {
    val nameFlow: StateFlow<PageNameData?>
    val state: StateFlow<State>
    fun onIntent(intent: Intent)
}