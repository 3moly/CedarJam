package com.moly3.cedarjam.pages.page_collection_row

import androidx.compose.runtime.Immutable
import com.arkivanov.decompose.router.slot.ChildSlot
import com.arkivanov.decompose.value.Value
import com.moly3.cedarjam.features.feature_graph.IDialogGraphComponent
import com.moly3.cedarjam.core.ui.model.PageNameData
import com.moly3.cedarjam.core.domain.service.WorkspaceSession
import com.moly3.cedarjam.features.feature_graph.IDialogGraphContainer
import kotlinx.coroutines.flow.StateFlow

@Immutable
interface CollectionRowComponent: IDialogGraphContainer {
    val workspaceSession: WorkspaceSession
    val nameFlow: StateFlow<PageNameData?>
    val state: StateFlow<State>
    fun onIntent(intent: Intent)
}