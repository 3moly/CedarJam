package com.moly3.cedarjam.ui.pages.tag

import androidx.compose.runtime.Immutable
import com.arkivanov.decompose.router.slot.ChildSlot
import com.arkivanov.decompose.value.Value
import com.moly3.cedarjam.features.feature_graph.IDialogGraphComponent
import com.moly3.cedarjam.core.ui.model.PageNameData
import kotlinx.coroutines.flow.StateFlow

@Immutable
interface TagComponent {
    val nameFlow: StateFlow<PageNameData?>
    val state: StateFlow<State>
    fun onIntent(intent: Intent)
    val dialogSlot: Value<ChildSlot<*, IDialogGraphComponent>>
}