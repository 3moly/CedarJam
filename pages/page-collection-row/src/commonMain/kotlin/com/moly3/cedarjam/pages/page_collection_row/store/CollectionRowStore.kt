package com.moly3.cedarjam.pages.page_collection_row.store

import com.arkivanov.mvikotlin.core.store.Store
import com.moly3.cedarjam.pages.page_collection_row.Intent
import com.moly3.cedarjam.pages.page_collection_row.State
import com.moly3.cedarjam.core.domain.model.CollectionDTO
import com.moly3.cedarjam.core.domain.model.CollectionRowDTO
import com.moly3.cedarjam.core.ui.model.PageNameData
import com.moly3.cedarjam.core.domain.model.WorkspacePresentation
import kotlinx.coroutines.flow.StateFlow

internal interface CollectionRowStore : Store<Intent, State, Unit> {
    val nameStateFlow: StateFlow<PageNameData?>

    sealed interface Msg {
        data class SetCollection(val value: CollectionDTO?) : Msg
        data class SetCollectionRow(val value: CollectionRowDTO?) : Msg
    }
}
