package com.moly3.cedarjam.pages.page_collection.store

import com.arkivanov.mvikotlin.core.store.Store
import com.moly3.cedarjam.pages.page_collection.Intent
import com.moly3.cedarjam.pages.page_collection.Label
import com.moly3.cedarjam.pages.page_collection.State
import com.moly3.cedarjam.core.domain.model.CollectionDTO
import com.moly3.cedarjam.core.domain.model.CollectionRowDTO
import com.moly3.cedarjam.core.ui.model.PageNameData
import com.moly3.cedarjam.core.domain.model.TagCollectionRowDTO
import com.moly3.cedarjam.core.domain.model.TagDTO
import com.moly3.cedarjam.core.domain.model.WorkspacePresentation
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.StateFlow

internal interface CollectionStore : Store<Intent, State, Label> {
    val nameStateFlow: StateFlow<PageNameData?>

    sealed interface Msg {
        data class SetWorkspace(val value: WorkspacePresentation?) : Msg
        data class SetCollection(val value: CollectionDTO?) : Msg
        data class SetTagCollectionRows(val value: List<TagCollectionRowDTO>) : Msg
        data class SetRows(val value: List<CollectionRowDTO>) : Msg
        data class SetTags(val value: ImmutableList<TagDTO>) : Msg
        data class SetCurrentPage(val value: Long) : Msg
        data class SetMaxPage(val value: Long) : Msg
    }
}
