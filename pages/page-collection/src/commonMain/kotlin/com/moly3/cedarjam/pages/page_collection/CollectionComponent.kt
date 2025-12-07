package com.moly3.cedarjam.pages.page_collection

import androidx.compose.runtime.Immutable
import com.moly3.cedarjam.core.ui.model.PageNameData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

@Immutable
interface CollectionComponent {
    val nameFlow: StateFlow<PageNameData?>
    val state: StateFlow<State>
    val labels: Flow<Label>
    fun onIntent(intent: Intent)
}