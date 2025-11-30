package com.moly3.cedarjam.ui.pages.tag.store

import com.arkivanov.mvikotlin.core.store.Store
import com.moly3.cedarjam.ui.pages.tag.Intent
import com.moly3.cedarjam.ui.pages.tag.State
import com.moly3.cedarjam.core.domain.model.PageNameData
import com.moly3.cedarjam.core.domain.model.TagDTO
import com.moly3.cedarjam.core.domain.model.UIState
import com.moly3.cedarjam.core.domain.model.node.ObsidianGraphPresentation
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.StateFlow

internal interface TagStore : Store<Intent, State, Unit> {
    val nameStateFlow: StateFlow<PageNameData?>

    sealed interface Msg {
        data class SetTagState(val value: UIState<TagDTO, Unit>) : Msg
        data class SetConnections(val value: ImmutableList<ObsidianGraphPresentation>) : Msg
    }
}
